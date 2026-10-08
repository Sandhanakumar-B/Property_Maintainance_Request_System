# Property Maintenance Request System

**Software Engineering SAT II Assessment – Working Prototype**

A clean, layered, beginner-friendly web application developed with **Java Spring Boot**, **MySQL / Spring Data JPA**, and **Vanilla HTML5, CSS3, and JavaScript**.

---

## 📖 1. Project Description

The **Property Maintenance Request System** facilitates seamless maintenance tracking for residential and commercial properties. It allows tenants and property owners (**Users**) to submit, view, track, and cancel maintenance tickets while strictly enforcing capacity limits, eligibility rules, dynamic fee calculations, and immutable historical records. Property managers (**Administrators**) can supervise system resources and advance request lifecycles through strict, sequential status transitions.

---

## 🏗️ 2. System Architecture

The application follows a standard **Layered Architecture** adhering to separation of concerns:

```
+-------------------------------------------------------------+
|               Client Browser (HTML / CSS / JS)              |
+-------------------------------------------------------------+
                              │ HTTP / REST Calls
                              ▼
+-------------------------------------------------------------+
|                      Controller Layer                       |
|   (UserController, ResourceController, RequestController)   |
+-------------------------------------------------------------+
                              │ Calls
                              ▼
+-------------------------------------------------------------+
|                       Service Layer                         |
|   ValidationService (Validates business rules BR1 - BR6)    |
|   RequestService    (Handles transactions, workflows BR7-10)|
+-------------------------------------------------------------+
                              │ Database CRUD
                              ▼
+-------------------------------------------------------------+
|                      Repository Layer                       |
|   (UserRepository, ResourceRepository,                      |
|    ServiceRequestRepository, RequestStatusHistoryRepository) |
+-------------------------------------------------------------+
                              │
                              ▼
+-------------------------------------------------------------+
|                      Database (MySQL / H2)                  |
|   (users, resources, service_requests,                      |
|    request_status_history)                                  |
+-------------------------------------------------------------+
```

---

## ⚡ 3. Key Features & Business Rules Satisfied

* **FR1 / FR4 / FR5 – Maintenance Ticket Creation**: Generates unique maintenance tickets with detailed property reference and service specifications.
* **BR1 / FR2 – Active Request Limit**: Restricts each user to a maximum of **5 active requests** (`SUBMITTED`, `APPROVED/ASSIGNED`, `IN_PROGRESS`). The 6th request is rejected.
* **BR2 – User & Service Eligibility**: Verifies that the requester is `ACTIVE` and that the selected service resource is `ACTIVE` with `availableCapacity > 0`.
* **BR3 – Immutable Historical Snapshot**: Persists a complete JSON snapshot of the user details, service parameters, and calculated charges at submission time.
* **BR4 / FR3 – Real-time Applicable Charge Calculation**:
  * **Rs. 50.00** when chargeable amount is below Rs. 1,000.
  * **Rs. 0.00** when chargeable amount is Rs. 1,000 or above (or when no charge applies).
* **BR5 – Details & Phone Validation**: Validates that name, reference, and description are non-blank, and phone numbers contain exactly **10 numeric digits**.
* **BR7 – Atomic Capacity Reservation & Audit Log**: Decrements service capacity atomically upon submission and writes an initial audit history entry.
* **BR8 / FR7 – Sequential Status Advancement (Admin)**: Strict status transitions:
  $$\text{SUBMITTED} \longrightarrow \text{APPROVED/ASSIGNED} \longrightarrow \text{IN\_PROGRESS} \longrightarrow \text{COMPLETED}$$
  *Skipping lifecycle stages is strictly rejected.*
* **BR9 / FR8 – Request Cancellation & Capacity Release**: Users can cancel requests in `SUBMITTED` status. Cancellation releases reserved capacity back to the resource pool exactly once.
* **BR10 / FR6 – Request Access Control**: Users can only view and cancel their own requests; Administrators can supervise all requests.

---

## 🛠️ 4. Technologies Used

* **Backend**: Java 17, Spring Boot 3.2.3 (Spring Web, Spring Data JPA)
* **Frontend**: HTML5, CSS3, JavaScript (Fetch API, responsive CSS)
* **Database**: MySQL 8.x (Production) & H2 in-memory database (Automated test profile)
* **Build Tool**: Apache Maven 3.x
* **Testing**: JUnit 5, Spring Boot Test (`@SpringBootTest`)

---

## 👥 5. Sample User Accounts & Seed Data

The application automatically seeds the following accounts and resources on startup:

### User Accounts
| Role | Account Name | Phone | Email | Status |
| :--- | :--- | :--- | :--- | :--- |
| **User** | `USER1` | `0771234567` | `user1@property.lk` | `ACTIVE` |
| **User** | `USER2` | `0777654321` | `user2@property.lk` | `ACTIVE` |
| **Administrator** | `ADMIN1` | `0770000000` | `admin@property.lk` | `ACTIVE` |

### Maintenance Services / Resources
| Code | Service Name | Total Capacity | Available Capacity | Status |
| :--- | :--- | :---: | :---: | :---: |
| `S101` | Electrical Maintenance | 10 | 10 | `ACTIVE` |
| `S102` | Plumbing & Drainage | 5 | 5 | `ACTIVE` |
| `S103` | HVAC & Air Conditioning | 0 | 0 | `INACTIVE` |

---

## 🚀 6. How to Set Up and Run

### Prerequisites
* **Java Development Kit (JDK 17 or higher)**
* **Apache Maven** (or included Maven wrapper)
* **MySQL Server** (Optional for testing, H2 is built-in for automated tests)

### Step 1: Database Setup (MySQL)
Create the MySQL database in your MySQL client:
```sql
CREATE DATABASE IF NOT EXISTS property_maintenance_db;
```

Update your MySQL username and password in `src/main/resources/application.properties` if different from `root`/`root`:
```properties
spring.datasource.username=root
spring.datasource.password=root
```

### Step 2: Build the Application
Open a terminal in the project root and run:
```bash
mvn clean compile
```

### Step 3: Run the Application
Start the Spring Boot server:
```bash
mvn spring-boot:run
```

### Step 4: Access the Web Interface
Open your browser and navigate to:
```text
http://localhost:8080/
```

---

## 🧪 7. Test Cases & Verification (T1 – T12)

Run all 12 mandatory unit and integration tests using Maven:
```bash
mvn test
```

### Test Case Matrix:
| ID | Test Name | Business Rule | Verification |
| :---: | :--- | :---: | :--- |
| **T1** | Active Request Limit | BR1 | Creates 5 active requests; verifies 6th is rejected. |
| **T2** | Inactive User Rejection | BR2 | Verifies inactive user cannot submit tickets. |
| **T3** | Inactive Resource Rejection | BR2 | Verifies requests targeting `S103` (INACTIVE) are rejected. |
| **T4** | Zero Capacity Rejection | BR2 | Verifies request for resource with 0 available slots is rejected. |
| **T5** | Charge Calculation (< Rs. 1,000) | BR4 | Verifies Rs. 250 / Rs. 999.99 amounts incur Rs. 50.00 charge. |
| **T6** | Charge Calculation (&ge; Rs. 1,000) | BR4 | Verifies Rs. 1,000 / Rs. 5,000 amounts result in Rs. 0.00 charge. |
| **T7** | Charge Calculation (Zero / Null) | BR4 | Verifies 0 or null chargeable amount results in Rs. 0.00 charge. |
| **T8** | Phone Number Validation | BR5 | Verifies 10-digit format; rejects non-10 digit inputs. |
| **T9** | Request Creation & Capacity Decrement | BR6, BR7 | Verifies status is `SUBMITTED`, snapshot created, and capacity decreased by 1. |
| **T10** | Audit Trail History | FR5, BR7 | Verifies status transition logs are recorded with timestamp and actor. |
| **T11** | Status Transition Enforcement | BR8 | Verifies valid transitions (`SUBMITTED` &rarr; `APPROVED/ASSIGNED` &rarr; `IN_PROGRESS` &rarr; `COMPLETED`) and rejects invalid skips. |
| **T12** | Request Cancellation & Capacity Release | BR8, BR9 | Verifies cancelling a `SUBMITTED` request releases capacity; rejects cancelling non-SUBMITTED or duplicate cancellations. |

---

## 🧭 8. Demonstration Workflows for Viva

### User Workflow:
1. Open `http://localhost:8080/` and click **"Login as USER1"**.
2. From the User Dashboard, click **"➕ Submit New Request"**.
3. Select `S101` (Electrical Maintenance), enter a reference (e.g. `Unit 4B`), description, and chargeable amount (e.g. `500.00`).
4. Observe the **live fee preview** showing `Rs. 50.00`. Click **"Confirm & Submit"**.
5. View the ticket details page showing the **Preserved Snapshot Data** and initial **Audit Trail**.
6. Navigate to **"My Requests"** and click **"Cancel"** on the ticket &rarr; Confirm cancellation and observe status change to `CANCELLED`.

### Admin Workflow:
1. Open `http://localhost:8080/` and click **"Login as ADMIN1"**.
2. Observe the **Resource Capacity Monitor** on the Admin Dashboard showing real-time slot usage.
3. Navigate to **"Manage Requests"**.
4. Advance any `SUBMITTED` request step-by-step:
   * Click **"Approve &rarr;"** &rarr; moves to `APPROVED/ASSIGNED`
   * Click **"Start Work &rarr;"** &rarr; moves to `IN_PROGRESS`
   * Click **"Complete ✅"** &rarr; moves to `COMPLETED`
5. Click **"Details & Audit"** to view the full audit history showing each actor and timestamp.

---

## 📌 9. Known Limitations & Future Improvements

### Limitations:
* Authentication is simulated for rapid viva presentation without session tokens or JWTs.
* Real-time notifications (WebSockets / SMS) are not included to keep the architecture clean and lightweight.

### Future Improvements:
* Add Spring Security with JWT tokens and BCrypt password encryption for multi-tenant production use.
* Introduce automated email/SMS dispatch upon status changes.
* Attach digital photo uploads for maintenance damage verification.
