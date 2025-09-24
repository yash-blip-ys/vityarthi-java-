# Campus Course & Records Manager (CCRM)

A comprehensive Java-based student information system for managing academic records, course enrollments, and grade tracking. The system provides a command-line interface for administrators to manage students, courses, enrollments, and generate academic transcripts.

## Features

### Core Functionality
- **Student Management**: Register, view, and manage student profiles with unique registration numbers
- **Course Management**: Create and manage courses with course codes, credits, instructors, and semesters
- **Enrollment System**: Enroll students in courses with credit limit validation (18 credits maximum)
- **Grade Management**: Record and track student grades using S/A/B/C/D/E/F grading system
- **Transcript Generation**: Generate formatted academic transcripts with GPA calculations
- **Data Import/Export**: CSV-based data import and export functionality
- **Backup System**: Automated backup creation with timestamp-based versioning

### Advanced Features
- **Search Functionality**: Search courses by title or course code
- **Credit Validation**: Automatic validation to prevent students from exceeding 18-credit limit
- **GPA Calculation**: Automatic calculation of cumulative GPA based on grades and credits
- **File System Simulation**: In-memory file system for data persistence simulation
- **Exception Handling**: Comprehensive error handling for various scenarios

## System Architecture

### Package Structure
```
CCRM_Application/
├── config/           # Configuration management
├── domain/           # Core entities (Student, Course, Enrollment, etc.)
├── service/          # Business logic services
├── io/               # File operations and data persistence
├── cli/              # Command-line interface
├── util/             # Utility classes and helper functions
└── exception/        # Custom exception classes
```

### Core Classes

#### Domain Models
- **Student**: Represents student with ID, registration number, contact info, and enrollment status
- **Course**: Represents academic courses with course codes, credits, instructors, and semesters
- **Instructor**: Represents faculty members with department information
- **Enrollment**: Links students to courses with grade tracking
- **Grade**: Enum for grade values (S=10.0, A=9.0, B=8.0, C=7.0, D=6.0, E=5.0, F=0.0)

#### Services
- **StudentService**: Manages student data and operations
- **CourseService**: Handles course management and search functionality
- **EnrollmentService**: Manages student enrollments and credit validation
- **TranscriptService**: Generates formatted academic transcripts

## Installation & Setup

### Prerequisites
- Java Development Kit (JDK) 8 or higher
- Command-line terminal or Java IDE

### Running the Application

#### Option 1: Command Line
```bash
# Compile the Java file
javac CCRM_Application.java

# Run the application
java CCRM_Application
```

#### Option 2: Online IDEs (Recommended)
- **Replit** (replit.com) - Best for full project management
- **JDoodle** (jdoodle.com) - Good for quick testing
- **OnlineGDB** (onlinegdb.com) - Supports debugging

## Usage Guide

### Main Menu Navigation
The system provides a menu-driven interface with the following options:

1. **Manage Students** - View all registered students and their profiles
2. **Manage Courses** - List courses and search by title/code
3. **Manage Enrollments & Grades** - Enroll students, record grades, view transcripts
4. **File Operations** - Export data and create backups
5. **Show Java Platform Info** - Display system information

### Sample Data
The system comes pre-loaded with sample data:

**Students:**
- Alice Johnson (B23001) - alice@example.com
- Bob Smith (B23002) - bob@example.com  
- Charlie Brown (A22105) - charlie@example.com

**Courses:**
- CS101: Intro to Programming (3 credits, Fall)
- MA201: Calculus I (4 credits, Fall)
- PY105: Modern Physics (3 credits, Spring)

**Instructors:**
- Dr. Evelyn Reed (Computer Science)
- Dr. Samuel Tan (Physics & Math)

### Key Operations

#### Enrolling a Student
1. Select option 3 from main menu
2. Choose "Enroll Student in Course"
3. Enter Student ID (e.g., s001)
4. Enter Course Code (e.g., CS101)
5. System validates credit limits and enrollment status

#### Recording Grades
1. Select option 3 from main menu
2. Choose "Record Grade"
3. Enter Student ID and Course Code
4. Enter grade (S, A, B, C, D, E, F)

#### Viewing Transcripts
1. Select option 3 from main menu
2. Choose "View Student Transcript"
3. Enter Student ID
4. System displays formatted transcript with GPA

## Technical Details

### Java Platform Information
- **Java SE (Standard Edition)**: Used for general-purpose desktop applications
- **JDK vs JRE vs JVM**:
  - JDK: Development Kit with compilation tools
  - JRE: Runtime Environment with libraries and JVM
  - JVM: Virtual Machine that executes bytecode

### Grading System
The system uses a 10-point grading scale:
- S: 10.0 points (Outstanding)
- A: 9.0 points (Excellent)
- B: 8.0 points (Very Good)
- C: 7.0 points (Good)
- D: 6.0 points (Satisfactory)
- E: 5.0 points (Pass)
- F: 0.0 points (Fail)

### Credit System
- Maximum credits per student: 18
- System automatically validates credit limits during enrollment
- Prevents over-enrollment through exception handling

## File Structure

### Data Files (CSV Format)
- `students.csv`: Student information and profiles
- `courses.csv`: Course catalog with instructor assignments
- `instructors.csv`: Faculty information and departments

### Directory Structure
```
data/         # Source data files
exports/      # Exported data files
backups/      # Timestamped backup files
```

## Error Handling

The system includes comprehensive exception handling for:
- `StudentNotFoundException`: When student ID is not found
- `CourseNotFoundException`: When course code is not found
- `DuplicateEnrollmentException`: When attempting duplicate enrollment
- `MaxCreditLimitExceededException`: When credit limit would be exceeded

## Future Enhancements

Potential improvements and extensions:
- Web-based user interface
- Database integration (MySQL/PostgreSQL)
- Advanced reporting and analytics
- Email notification system
- Multi-semester transcript support
- Course prerequisite management
- Fee management integration

## Contributing

To contribute to this project:
1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

## License

This project is developed for educational purposes. Feel free to use and modify for learning and non-commercial purposes.

## Support

For questions or issues:
- Review the code documentation
- Check the sample data and menu options
- Test with the provided sample scenarios
- Refer to the error messages for troubleshooting guidance

---
<img width="1404" height="1240" alt="image" src="https://github.com/user-attachments/assets/28c4f34e-ecda-460b-abe3-0a414da73372" />
<img width="1408" height="1425" alt="Screenshot 2025-09-24 230550" src="https://github.com/user-attachments/assets/7c8babb4-6984-4ad3-befa-43dab7af02ab" />
<img width="1404" height="1239" alt="Screenshot 2025-09-24 230656" src="https://github.com/user-attachments/assets/4545ee3d-a4b5-4509-8e06-4af53fc13094" />
<img width="1408" height="1343" alt="Screenshot 2025-09-24 230621" src="https://github.com/user-attachments/assets/24dd119a-4c80-4dfa-ae89-a26d21c481ff" />



