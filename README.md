Campus Course & Records Manager (CCRM)
A comprehensive Java-based student information system for managing academic records, course enrollments, and grade tracking. The system provides a command-line interface for administrators to manage students, courses, enrollments, and generate academic transcripts.

Features
Core Functionality
Student Management: Register, view, and manage student profiles with unique registration numbers

Course Management: Create and manage courses with course codes, credits, instructors, and semesters

Enrollment System: Enroll students in courses with credit limit validation (18 credits maximum)

Grade Management: Record and track student grades using S/A/B/C/D/E/F grading system

Transcript Generation: Generate formatted academic transcripts with GPA calculations

Data Import/Export: CSV-based data import and export functionality

Backup System: Automated backup creation with timestamp-based versioning

Advanced Features
Search Functionality: Search courses by title or course code

Credit Validation: Automatic validation to prevent students from exceeding 18-credit limit

GPA Calculation: Automatic calculation of cumulative GPA based on grades and credits

File System Simulation: In-memory file system for data persistence simulation

Exception Handling: Comprehensive error handling for various scenarios

System Architecture
Package Structure
text
CCRM_Application/
├── config/           # Configuration management
├── domain/           # Core entities (Student, Course, Enrollment, etc.)
├── service/          # Business logic services
├── io/               # File operations and data persistence
├── cli/              # Command-line interface
├── util/             # Utility classes and helper functions
└── exception/        # Custom exception classes
<img width="1408" height="1426" alt="image" src="https://github.com/user-attachments/assets/dad48a3e-2251-4a24-93a6-bd3324986059" />
<img width="1408" height="1344" alt="image" src="https://github.com/user-attachments/assets/e7618be8-53bc-4929-ad23-94e734df68a9" />
<img width="1404" height="1240" alt="image" src="https://github.com/user-attachments/assets/5d8a1466-40ae-41ca-8b10-03aefcf5be11" />


