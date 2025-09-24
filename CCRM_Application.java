import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CCRM_Application {

    public static class config {
        public static final class AppConfig {
            private static final AppConfig INSTANCE = new AppConfig();
            private final Path dataDirectory = Paths.get("data");
            private final Path backupDirectory = Paths.get("backups");
            private final Path exportsDirectory = Paths.get("exports");

            private AppConfig() {}

            public static AppConfig getInstance() {
                return INSTANCE;
            }

            public Path getDataDirectory() { return dataDirectory; }
            public Path getBackupDirectory() { return backupDirectory; }
            public Path getExportsDirectory() { return exportsDirectory; }
        }
    }

    public static class exception {
        public static class CourseNotFoundException extends Exception {
            public CourseNotFoundException(String message) { super(message); }
        }

        public static class StudentNotFoundException extends Exception {
            public StudentNotFoundException(String message) { super(message); }
        }

        public static class DuplicateEnrollmentException extends Exception {
            public DuplicateEnrollmentException(String message) { super(message); }
        }

        public static class MaxCreditLimitExceededException extends Exception {
            public MaxCreditLimitExceededException(String message) { super(message); }
        }
    }

    public static class domain {
        public enum Grade {
            S(10.0), A(9.0), B(8.0), C(7.0), D(6.0), E(5.0), F(0.0), NOT_GRADED(-1.0);
            private final double gradePoint;
            Grade(double gradePoint) { this.gradePoint = gradePoint; }
            public double getGradePoint() { return gradePoint; }
        }

        public enum Semester { SPRING, SUMMER, FALL, WINTER }

        public static final class CourseCode {
            private final String department;
            private final int number;

            public CourseCode(String department, int number) {
                assert department != null && !department.isBlank() : "Department cannot be null or blank";
                assert number >= 100 && number <= 999 : "Course number must be between 100 and 999";
                this.department = department;
                this.number = number;
            }

            @Override
            public String toString() { return department + number; }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                CourseCode that = (CourseCode) o;
                return number == that.number && department.equals(that.department);
            }

            @Override
            public int hashCode() { return Objects.hash(department, number); }
        }

        public abstract static class Person {
            protected final String id;
            protected String fullName;
            protected String email;

            public Person(String id, String fullName, String email) {
                this.id = id;
                this.fullName = fullName;
                this.email = email;
            }

            public abstract String getProfile();

            public String getId() { return id; }
            public String getFullName() { return fullName; }
            public void setFullName(String fullName) { this.fullName = fullName; }
            public String getEmail() { return email; }
            public void setEmail(String email) { this.email = email; }
        }

        public static class Instructor extends Person {
            private String department;

            public Instructor(String id, String fullName, String email, String department) {
                super(id, fullName, email);
                this.department = department;
            }
            @Override
            public String getProfile() {
                return String.format("Instructor: %s (%s) - Dept: %s", fullName, email, department);
            }
            public String getDepartment() { return department; }
        }

        public static class Student extends Person {
            private final String regNo;
            private final LocalDate enrollmentDate;
            private boolean active = true;

            public Student(String id, String regNo, String fullName, String email) {
                super(id, fullName, email);
                this.regNo = regNo;
                this.enrollmentDate = LocalDate.now();
            }

            @Override
            public String getProfile() {
                return String.format("Student: %s (Reg# %s) | Status: %s | Enrolled: %s",
                        fullName, regNo, active ? "Active" : "Inactive", enrollmentDate);
            }

            public String getRegNo() { return regNo; }
            public boolean isActive() { return active; }
            public void setActive(boolean active) { this.active = active; }
        }

        public static class Course {
            private final CourseCode courseCode;
            private final String title;
            private final int credits;
            private Instructor instructor;
            private final Semester semester;

            private Course(Builder builder) {
                this.courseCode = builder.courseCode;
                this.title = builder.title;
                this.credits = builder.credits;
                this.instructor = builder.instructor;
                this.semester = builder.semester;
            }

            public CourseCode getCourseCode() { return courseCode; }
            public String getTitle() { return title; }
            public int getCredits() { return credits; }
            public Instructor getInstructor() { return instructor; }
            public Semester getSemester() { return semester; }
            public void setInstructor(Instructor instructor) { this.instructor = instructor; }
            public String getInstructorName() { return instructor != null ? instructor.getFullName() : "TBD"; }
            public String getDepartment() { return courseCode.department; }

            @Override
            public String toString() {
                return String.format("Course[%s]: %s (%d credits) | Instructor: %s | Semester: %s",
                        courseCode, title, credits, getInstructorName(), semester);
            }
            
            public static class Builder {
                private final CourseCode courseCode;
                private final String title;
                private int credits = 3;
                private Instructor instructor;
                private Semester semester = Semester.FALL;

                public Builder(CourseCode courseCode, String title) {
                    this.courseCode = courseCode;
                    this.title = title;
                }

                public Builder credits(int credits) {
                    this.credits = credits;
                    return this;
                }

                public Builder instructor(Instructor instructor) {
                    this.instructor = instructor;
                    return this;
                }

                public Builder semester(Semester semester) {
                    this.semester = semester;
                    return this;
                }

                public Course build() {
                    return new Course(this);
                }
            }
        }

        public static class Enrollment {
            private final Student student;
            private final Course course;
            private Grade grade;

            public Enrollment(Student student, Course course) {
                this.student = student;
                this.course = course;
                this.grade = Grade.NOT_GRADED;
            }

            public Student getStudent() { return student; }
            public Course getCourse() { return course; }
            public Grade getGrade() { return grade; }
            public void setGrade(Grade grade) { this.grade = grade; }

            @Override
            public String toString() {
                return String.format("  - %s: %s | Grade: %s", course.getCourseCode(), course.getTitle(), grade);
            }
        }
    }

    public static class util {
        public static class RecursiveUtils {
            public static long calculateDirectorySize(Path path, io.InMemoryFileSystem fs) {
                long totalSize = 0;
                try {
                    List<Path> children = fs.list(path);
                    for (Path child : children) {
                        if (fs.isDirectory(child)) {
                            totalSize += calculateDirectorySize(child, fs);
                        } else {
                            totalSize += fs.size(child);
                        }
                    }
                } catch (IOException e) {
                    System.err.println("Error calculating size for " + path + ": " + e.getMessage());
                }
                return totalSize;
            }
        }
        
        public static class MiscUtils {
             public static void demonstrateBitwiseOps() {
                System.out.println("\n--- Demonstrating Operator Precedence & Bitwise Ops ---");
                int a = 5;
                int b = 3;
                int result = (a & b) << 1;
                System.out.println("Expression: (a & b) << 1");
                System.out.println("Here, bitwise AND (&) has higher precedence than left shift (<<).");
                System.out.printf("(%d & %d) << 1 = %d\n", a, b, result);
                System.out.println("-------------------------------------------------------\n");
            }

            public static void demonstrateArrayUtils() {
                 System.out.println("\n--- Demonstrating java.util.Arrays ---");
                 String[] regNos = {"B23004", "B23001", "A22105", "C21030"};
                 System.out.println("Original Array: " + Arrays.toString(regNos));
                 Arrays.sort(regNos);
                 System.out.println("Sorted Array:   " + Arrays.toString(regNos));
                 int index = Arrays.binarySearch(regNos, "B23001");
                 System.out.println("Binary Search for 'B23001': Found at index " + index);
                 System.out.println("--------------------------------------\n");
            }
        }
    }
    
    public static class io {
        public static class InMemoryFileSystem {
            private final Map<Path, List<String>> files = new HashMap<>();
            private final Map<Path, Set<Path>> directories = new HashMap<>();

            public InMemoryFileSystem() {
                Path root = Paths.get("");
                directories.put(root, new HashSet<>());
                
                try {
                    Path dataDir = config.AppConfig.getInstance().getDataDirectory();
                    createDirectory(dataDir);
                    
                    Path studentsCsv = dataDir.resolve("students.csv");
                    List<String> studentData = List.of(
                        "id,regNo,fullName,email",
                        "s001,B23001,Alice Johnson,alice@example.com",
                        "s002,B23002,Bob Smith,bob@example.com",
                        "s003,A22105,Charlie Brown,charlie@example.com"
                    );
                    write(studentsCsv, studentData);

                    Path coursesCsv = dataDir.resolve("courses.csv");
                     List<String> courseData = List.of(
                        "code,title,credits,semester,instructorId",
                        "CS101,Intro to Programming,3,FALL,i01",
                        "MA201,Calculus I,4,FALL,i02",
                        "PY105,Modern Physics,3,SPRING,i02"
                    );
                    write(coursesCsv, courseData);
                    
                     Path instructorsCsv = dataDir.resolve("instructors.csv");
                     List<String> instructorData = List.of(
                        "id,fullName,email,department",
                        "i01,Dr. Evelyn Reed,e.reed@example.com,Computer Science",
                        "i02,Dr. Samuel Tan,s.tan@example.com,Physics & Math"
                    );
                    write(instructorsCsv, instructorData);
                } catch (IOException e) {
                    System.err.println("Failed to initialize in-memory file system.");
                }
            }

            public void createDirectory(Path dir) throws FileAlreadyExistsException {
                if(directories.containsKey(dir) || files.containsKey(dir)) {
                    throw new FileAlreadyExistsException(dir.toString());
                }
                directories.put(dir, new HashSet<>());
                Path parent = dir.getParent();
                if (parent != null && directories.containsKey(parent)) {
                    directories.get(parent).add(dir);
                }
            }
            
            public boolean exists(Path path) { return files.containsKey(path) || directories.containsKey(path); }
            public boolean isDirectory(Path path) { return directories.containsKey(path); }
            public Stream<String> lines(Path path) throws IOException {
                if (!exists(path) || isDirectory(path)) throw new IOException("File not found: " + path);
                return files.get(path).stream();
            }
            public void write(Path path, Iterable<String> lines) throws IOException {
                List<String> content = new ArrayList<>();
                lines.forEach(content::add);
                files.put(path, content);
                Path parent = path.getParent();
                if (parent != null && directories.containsKey(parent)) {
                    directories.get(parent).add(path);
                }
            }
            public void copy(Path source, Path target) throws IOException {
                 if (!exists(source)) throw new IOException("Source does not exist: " + source);
                 if (isDirectory(source)) throw new IOException("Copying directories not supported in this simulation");
                 files.put(target, new ArrayList<>(files.get(source)));
                 Path parent = target.getParent();
                 if (parent != null && directories.containsKey(parent)) {
                    directories.get(parent).add(target);
                 }
            }
            public long size(Path path) throws IOException {
                if (!exists(path) || isDirectory(path)) throw new IOException("Cannot get size for: " + path);
                return files.get(path).stream().mapToLong(String::length).sum();
            }
            public List<Path> list(Path dir) throws IOException {
                if(!isDirectory(dir)) throw new IOException("Not a directory: " + dir);
                return new ArrayList<>(directories.get(dir));
            }
        }
        
        public static class ImportExportService {
            private final InMemoryFileSystem fs;
            public ImportExportService(InMemoryFileSystem fs) { this.fs = fs; }

            public Map<String, domain.Student> importStudents(Path path) throws IOException {
                try (Stream<String> lines = fs.lines(path)) {
                    return lines.skip(1)
                        .map(this::parseStudentFromCsv)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toMap(domain.Student::getId, s -> s));
                }
            }
             public Map<String, domain.Instructor> importInstructors(Path path) throws IOException {
                try (Stream<String> lines = fs.lines(path)) {
                    return lines.skip(1)
                        .map(this::parseInstructorFromCsv)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toMap(domain.Instructor::getId, i -> i));
                }
            }

            public Map<domain.CourseCode, domain.Course> importCourses(Path path, Map<String, domain.Instructor> instructors) throws IOException {
                 try (Stream<String> lines = fs.lines(path)) {
                     return lines.skip(1)
                        .map(line -> parseCourseFromCsv(line, instructors))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toMap(domain.Course::getCourseCode, c -> c));
                 }
            }

            public void exportStudents(Path path, Collection<domain.Student> students) throws IOException {
                List<String> lines = new ArrayList<>();
                lines.add("id,regNo,fullName,email,active");
                students.forEach(s -> lines.add(String.format("%s,%s,%s,%s,%b",
                    s.getId(), s.getRegNo(), s.getFullName(), s.getEmail(), s.isActive())));
                fs.write(path, lines);
            }

             public void exportCourses(Path path, Collection<domain.Course> courses) throws IOException {
                List<String> lines = new ArrayList<>();
                lines.add("code,title,credits,semester,instructorId");
                courses.forEach(c -> lines.add(String.format("%s,%s,%d,%s,%s",
                    c.getCourseCode().toString(), c.getTitle(), c.getCredits(), c.getSemester(),
                    c.getInstructor() != null ? c.getInstructor().getId() : "N/A")));
                fs.write(path, lines);
            }

            private domain.Student parseStudentFromCsv(String line) {
                String[] parts = line.split(",");
                if (parts.length < 4) return null;
                return new domain.Student(parts[0].trim(), parts[1].trim(), parts[2].trim(), parts[3].trim());
            }

             private domain.Instructor parseInstructorFromCsv(String line) {
                String[] parts = line.split(",");
                if (parts.length < 4) return null;
                return new domain.Instructor(parts[0].trim(), parts[1].trim(), parts[2].trim(), parts[3].trim());
            }

            private domain.Course parseCourseFromCsv(String line, Map<String, domain.Instructor> instructors) {
                String[] parts = line.split(",");
                if (parts.length < 5) return null;
                try {
                    String codeStr = parts[0].trim();
                    String dept = codeStr.replaceAll("[0-9]", "");
                    int num = Integer.parseInt(codeStr.replaceAll("[^0-9]", ""));
                    domain.CourseCode code = new domain.CourseCode(dept, num);

                    domain.Instructor instructor = instructors.get(parts[4].trim());

                    return new domain.Course.Builder(code, parts[1].trim())
                            .credits(Integer.parseInt(parts[2].trim()))
                            .semester(domain.Semester.valueOf(parts[3].trim().toUpperCase()))
                            .instructor(instructor)
                            .build();
                } catch (Exception e) {
                    System.err.println("Skipping invalid course line: " + line);
                    return null;
                }
            }
        }
        
        public static class BackupService {
            private final InMemoryFileSystem fs;
            private final config.AppConfig appConfig = config.AppConfig.getInstance();

            public BackupService(InMemoryFileSystem fs) { this.fs = fs; }

            public void performBackup() {
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                Path backupSubDir = appConfig.getBackupDirectory().resolve("backup_" + timestamp);
                
                System.out.println("\n>>> Starting backup to " + backupSubDir + "...");

                try {
                    if (!fs.exists(appConfig.getBackupDirectory())) {
                         fs.createDirectory(appConfig.getBackupDirectory());
                    }
                    fs.createDirectory(backupSubDir);

                    Path sourceDir = appConfig.getExportsDirectory();
                    if(!fs.exists(sourceDir) || !fs.isDirectory(sourceDir)) {
                        System.out.println("No export files to backup. Please export data first.");
                        return;
                    }
                    
                    for (Path sourceFile : fs.list(sourceDir)) {
                         if (!fs.isDirectory(sourceFile)) {
                            Path targetFile = backupSubDir.resolve(sourceFile.getFileName());
                            fs.copy(sourceFile, targetFile);
                            System.out.println("  - Backed up " + sourceFile + " to " + targetFile);
                         }
                    }
                    System.out.println(">>> Backup completed successfully.");
                } catch (IOException e) {
                    System.err.println("!!! Backup failed: " + e.getMessage());
                } finally {
                    System.out.println(">>> Backup process finished.");
                }
            }
        }
    }

    public static class service {
        public interface Persistable {
            void loadData() throws IOException;
            void saveData() throws IOException;
        }

        public interface Searchable<T> {
             List<T> search(String query);
             default void displaySearchResults(List<T> results) {
                if (results.isEmpty()) {
                    System.out.println("No results found.");
                } else {
                    results.forEach(System.out::println);
                }
             }
        }
        
        public static class StudentService implements Persistable {
            private final Map<String, domain.Student> students = new HashMap<>();
            private final io.ImportExportService ioService;
             private final config.AppConfig appConfig = config.AppConfig.getInstance();

            public StudentService(io.ImportExportService ioService) {
                this.ioService = ioService;
            }

            @Override
            public void loadData() throws IOException {
                Path studentsPath = appConfig.getDataDirectory().resolve("students.csv");
                students.clear();
                students.putAll(ioService.importStudents(studentsPath));
                System.out.println("Loaded " + students.size() + " students.");
            }

            @Override
            public void saveData() throws IOException {
                 if (!ioService.fs.exists(appConfig.getExportsDirectory())) {
                    ioService.fs.createDirectory(appConfig.getExportsDirectory());
                }
                Path studentsPath = appConfig.getExportsDirectory().resolve("students.csv");
                ioService.exportStudents(studentsPath, students.values());
                System.out.println("Exported " + students.size() + " students to " + studentsPath);
            }

            public void addStudent(domain.Student s) { students.put(s.getId(), s); }
            public Optional<domain.Student> findStudentById(String id) { return Optional.ofNullable(students.get(id)); }
             public List<domain.Student> getAllStudents() {
                return new ArrayList<>(students.values());
            }

            public void printStudentProfile(String studentId) throws exception.StudentNotFoundException {
                domain.Person p = findStudentById(studentId)
                    .orElseThrow(() -> new exception.StudentNotFoundException("Student not found: " + studentId));

                if (p instanceof domain.Student) {
                    domain.Student s = (domain.Student) p;
                    System.out.println("\n--- Student Profile ---");
                    System.out.println(s.getProfile());
                    System.out.println("-----------------------");
                }
            }
        }

        public static class CourseService implements Persistable, Searchable<domain.Course> {
             private final Map<domain.CourseCode, domain.Course> courses = new HashMap<>();
             private Map<String, domain.Instructor> instructors = new HashMap<>();
             private final io.ImportExportService ioService;
             private final config.AppConfig appConfig = config.AppConfig.getInstance();

            public CourseService(io.ImportExportService ioService) { this.ioService = ioService; }

            @Override
            public void loadData() throws IOException {
                Path instructorsPath = appConfig.getDataDirectory().resolve("instructors.csv");
                instructors = ioService.importInstructors(instructorsPath);
                
                Path coursesPath = appConfig.getDataDirectory().resolve("courses.csv");
                courses.clear();
                courses.putAll(ioService.importCourses(coursesPath, instructors));
                System.out.println("Loaded " + courses.size() + " courses and " + instructors.size() + " instructors.");
            }

            @Override
            public void saveData() throws IOException {
                if (!ioService.fs.exists(appConfig.getExportsDirectory())) {
                    ioService.fs.createDirectory(appConfig.getExportsDirectory());
                }
                Path coursesPath = appConfig.getExportsDirectory().resolve("courses.csv");
                ioService.exportCourses(coursesPath, courses.values());
                System.out.println("Exported " + courses.size() + " courses to " + coursesPath);
            }
            
            public Optional<domain.Course> findCourseByCode(domain.CourseCode code) { return Optional.ofNullable(courses.get(code)); }
            public List<domain.Course> getAllCourses() { return new ArrayList<>(courses.values()); }
            public List<domain.Instructor> getAllInstructors() { return new ArrayList<>(instructors.values()); }
            
            public List<domain.Course> findCourses(domain.Semester semester) {
                return findCourses(c -> c.getSemester().equals(semester));
            }
            public List<domain.Course> findCourses(Predicate<domain.Course> filter) {
                return courses.values().stream()
                    .filter(filter)
                    .collect(Collectors.toList());
            }

            @Override
            public List<domain.Course> search(String query) {
                String lowerQuery = query.toLowerCase();
                return findCourses(c -> c.getTitle().toLowerCase().contains(lowerQuery) ||
                                        c.getCourseCode().toString().toLowerCase().contains(lowerQuery));
            }
        }
        
        public static class EnrollmentService {
            private final List<domain.Enrollment> enrollments = new ArrayList<>();
            private final int MAX_CREDITS = 18;

            public void enrollStudent(domain.Student student, domain.Course course) throws exception.MaxCreditLimitExceededException, exception.DuplicateEnrollmentException {
                boolean alreadyEnrolled = enrollments.stream()
                    .anyMatch(e -> e.getStudent().equals(student) && e.getCourse().equals(course));
                if (alreadyEnrolled) {
                    throw new exception.DuplicateEnrollmentException("Student " + student.getRegNo() + " is already enrolled in " + course.getCourseCode());
                }

                int currentCredits = getEnrollmentsForStudent(student.getId()).stream()
                    .mapToInt(e -> e.getCourse().getCredits())
                    .sum();
                
                if (currentCredits + course.getCredits() > MAX_CREDITS) {
                    throw new exception.MaxCreditLimitExceededException("Enrollment failed. Student would exceed max credit limit of " + MAX_CREDITS);
                }

                enrollments.add(new domain.Enrollment(student, course));
            }
            
            public List<domain.Enrollment> getEnrollmentsForStudent(String studentId) {
                return enrollments.stream()
                    .filter(e -> e.getStudent().getId().equals(studentId))
                    .collect(Collectors.toList());
            }

            public Optional<domain.Enrollment> findEnrollment(String studentId, domain.CourseCode code) {
                 return enrollments.stream()
                    .filter(e -> e.getStudent().getId().equals(studentId) && e.getCourse().getCourseCode().equals(code))
                    .findFirst();
            }
            
            public double calculateGpa(String studentId) {
                List<domain.Enrollment> studentEnrollments = getEnrollmentsForStudent(studentId);
                
                double totalPoints = studentEnrollments.stream()
                    .filter(e -> e.getGrade() != domain.Grade.NOT_GRADED)
                    .mapToDouble(e -> e.getGrade().getGradePoint() * e.getCourse().getCredits())
                    .sum();
                
                int totalCredits = studentEnrollments.stream()
                    .filter(e -> e.getGrade() != domain.Grade.NOT_GRADED)
                    .mapToInt(e -> e.getCourse().getCredits())
                    .sum();

                return totalCredits == 0 ? 0.0 : totalPoints / totalCredits;
            }

            public List<domain.Enrollment> getAllEnrollments() {
                return Collections.unmodifiableList(enrollments);
            }
        }

        public static class TranscriptService {
            public void printTranscript(domain.Student student, List<domain.Enrollment> enrollments, double gpa) {
                 System.out.println("\n========================================");
                 System.out.println("           ACADEMIC TRANSCRIPT          ");
                 System.out.println("========================================");
                 System.out.println(student.getProfile());
                 System.out.println("----------------------------------------");
                 if (enrollments.isEmpty()) {
                    System.out.println("No courses enrolled.");
                 } else {
                    enrollments.forEach(System.out::println);
                 }
                 System.out.println("----------------------------------------");
                 System.out.printf("Cumulative GPA: %.2f\n", gpa);
                 System.out.println("========================================\n");
            }
        }
    }

    public static class cli {
        public static class MenuHandler {
            private final Scanner scanner = new Scanner(System.in);
            private final service.StudentService studentService;
            private final service.CourseService courseService;
            private final service.EnrollmentService enrollmentService;
            private final service.TranscriptService transcriptService;
            private final io.BackupService backupService;
             private final io.InMemoryFileSystem fs;

            public MenuHandler() {
                this.fs = new io.InMemoryFileSystem();
                io.ImportExportService ioService = new io.ImportExportService(fs);
                this.studentService = new service.StudentService(ioService);
                this.courseService = new service.CourseService(ioService);
                this.enrollmentService = new service.EnrollmentService();
                this.transcriptService = new service.TranscriptService();
                this.backupService = new io.BackupService(fs);
            }

            public void start() {
                System.out.println("Welcome to the Campus Course & Records Manager (CCRM)");
                try {
                    studentService.loadData();
                    courseService.loadData();
                    System.out.println("Initial data loaded successfully from in-memory source.");
                } catch (IOException e) {
                    System.err.println("!!! Could not load initial data: " + e.getMessage());
                }
                
                util.MiscUtils.demonstrateBitwiseOps();
                util.MiscUtils.demonstrateArrayUtils();

                runMainMenu();
            }

            public void runMainMenu() {
                boolean running = true;
                do {
                    System.out.println("\n========= CCRM Main Menu =========");
                    System.out.println("1. Manage Students");
                    System.out.println("2. Manage Courses");
                    System.out.println("3. Manage Enrollments & Grades");
                    System.out.println("4. File Operations (Export/Backup)");
                    System.out.println("5. Show Java Platform Info");
                    System.out.println("0. Exit");
                    System.out.print("Enter your choice: ");

                    int choice = -1;
                    try {
                        choice = Integer.parseInt(scanner.nextLine());
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid input. Please enter a number.");
                        continue;
                    }

                    switch (choice) {
                        case 1 -> manageStudents();
                        case 2 -> manageCourses();
                        case 3 -> manageEnrollments();
                        case 4 -> manageFileOps();
                        case 5 -> showPlatformInfo();
                        case 0 -> running = false;
                        default -> System.out.println("Invalid choice. Please try again.");
                    }
                } while (running);
                System.out.println("\nThank you for using CCRM!");
            }
            
            private void manageStudents() {
                System.out.println("\n--- Student Management ---");
                studentService.getAllStudents().forEach(s -> System.out.println(s.getProfile()));
                System.out.println("--------------------------");
            }
            
            private void manageCourses() {
                System.out.println("\n--- Course Management ---");
                System.out.println("1. List All Courses");
                System.out.println("2. Search Courses");
                System.out.print("Enter choice: ");
                int choice = Integer.parseInt(scanner.nextLine());
                if (choice == 1) {
                    courseService.getAllCourses().stream()
                        .sorted(Comparator.comparing(c -> c.getCourseCode().toString()))
                        .forEach(System.out::println);
                } else if (choice == 2) {
                    searchCourses();
                }
            }

            private void searchCourses() {
                SEARCH_LOOP:
                for (int i = 0; i < 3; i++) {
                    System.out.print("\nEnter search query (title or code), or type 'back' to return: ");
                    String query = scanner.nextLine();

                    if ("back".equalsIgnoreCase(query)) {
                        break SEARCH_LOOP;
                    }
                    if (query.isBlank()) {
                        System.out.println("Query cannot be empty. Please try again.");
                        continue;
                    }

                    Predicate<domain.Course> advancedFilter = new Predicate<>() {
                        @Override
                        public boolean test(domain.Course course) {
                            return course.getCredits() > 1;
                        }
                    };

                    List<domain.Course> results = courseService.search(query).stream()
                        .filter(advancedFilter)
                        .collect(Collectors.toList());
                    
                    courseService.displaySearchResults(results);
                    return;
                }
                System.out.println("Returning to main menu.");
            }

            private void manageEnrollments() {
                System.out.println("\n--- Enrollment & Grades ---");
                System.out.println("1. Enroll Student in Course");
                System.out.println("2. Record Grade");
                System.out.println("3. View Student Transcript");
                System.out.print("Enter choice: ");
                int choice = Integer.parseInt(scanner.nextLine());
                switch (choice) {
                    case 1 -> enrollStudent();
                    case 2 -> recordGrade();
                    case 3 -> viewTranscript();
                }
            }
            
             private void enrollStudent() {
                try {
                    System.out.print("Enter Student ID (e.g., s001): ");
                    String studentId = scanner.nextLine();
                    domain.Student student = studentService.findStudentById(studentId)
                        .orElseThrow(() -> new exception.StudentNotFoundException("Student not found."));

                    System.out.print("Enter Course Code (e.g., CS101): ");
                    String codeStr = scanner.nextLine().toUpperCase();
                    domain.CourseCode code = new domain.CourseCode(codeStr.replaceAll("[0-9]",""), Integer.parseInt(codeStr.replaceAll("[^0-9]","")));
                    domain.Course course = courseService.findCourseByCode(code)
                        .orElseThrow(() -> new exception.CourseNotFoundException("Course not found."));
                    
                    enrollmentService.enrollStudent(student, course);
                    System.out.println("Enrollment successful!");

                } catch (exception.StudentNotFoundException | exception.CourseNotFoundException | exception.MaxCreditLimitExceededException | exception.DuplicateEnrollmentException e) {
                    System.err.println("!!! Error: " + e.getMessage());
                } catch (Exception e) {
                    System.err.println("!!! Invalid input format. " + e.getMessage());
                }
            }
            
            private void recordGrade() {
                 try {
                    System.out.print("Enter Student ID (e.g., s001): ");
                    String studentId = scanner.nextLine();
                    System.out.print("Enter Course Code (e.g., CS101): ");
                     String codeStr = scanner.nextLine().toUpperCase();
                    domain.CourseCode code = new domain.CourseCode(codeStr.replaceAll("[0-9]",""), Integer.parseInt(codeStr.replaceAll("[^0-9]","")));
                     
                    domain.Enrollment enrollment = enrollmentService.findEnrollment(studentId, code)
                         .orElseThrow(() -> new Exception("Student is not enrolled in this course."));

                    System.out.print("Enter Grade (S, A, B, C, D, E, F): ");
                    domain.Grade grade = domain.Grade.valueOf(scanner.nextLine().toUpperCase());
                    
                    enrollment.setGrade(grade);
                    System.out.println("Grade recorded successfully.");

                 } catch (Exception e) {
                    System.err.println("!!! Error: " + e.getMessage());
                 }
            }

            private void viewTranscript() {
                 try {
                    System.out.print("Enter Student ID to view transcript (e.g., s001): ");
                    String studentId = scanner.nextLine();
                    domain.Student student = studentService.findStudentById(studentId)
                         .orElseThrow(() -> new exception.StudentNotFoundException("Student not found."));

                    List<domain.Enrollment> enrollments = enrollmentService.getEnrollmentsForStudent(studentId);
                    double gpa = enrollmentService.calculateGpa(studentId);
                    
                    transcriptService.printTranscript(student, enrollments, gpa);

                 } catch (exception.StudentNotFoundException e) {
                     System.err.println("!!! Error: " + e.getMessage());
                 }
            }

            private void manageFileOps() {
                 System.out.println("\n--- File Operations ---");
                 System.out.println("1. Export All Data");
                 System.out.println("2. Create Backup from Exports");
                 System.out.println("3. Show Backup Size (Recursive Demo)");
                 System.out.print("Enter choice: ");
                 int choice = Integer.parseInt(scanner.nextLine());
                 try {
                     switch (choice) {
                         case 1 -> {
                             studentService.saveData();
                             courseService.saveData();
                         }
                         case 2 -> backupService.performBackup();
                         case 3 -> {
                            Path backupDir = config.AppConfig.getInstance().getBackupDirectory();
                            if (fs.exists(backupDir)) {
                                long size = util.RecursiveUtils.calculateDirectorySize(backupDir, fs);
                                System.out.printf("Total size of backup directory '%s' is %d simulated bytes.\n", backupDir, size);
                            } else {
                                System.out.println("Backup directory does not exist yet.");
                            }
                         }
                     }
                 } catch (IOException e) {
                    System.err.println("File operation failed: " + e.getMessage());
                 }
            }
             private void showPlatformInfo() {
                 System.out.println("\n--- Java Platform Information (as per README) ---");
                 System.out.println("\n** Java ME vs SE vs EE **");
                 System.out.println("- Java ME (Micro): For resource-constrained devices like embedded systems.");
                 System.out.println("- Java SE (Standard): For general-purpose desktop and server applications. This is what we are using.");
                 System.out.println("- Java EE (Enterprise): Extends Java SE with APIs for large-scale, multi-tiered, and web applications.");
                 System.out.println("\n** JDK > JRE > JVM **");
                 System.out.println("- JDK (Development Kit): Contains tools to CREATE Java apps (compiler, etc.).");
                 System.out.println("- JRE (Runtime Environment): Contains libraries and JVM to RUN Java apps.");
                 System.out.println("- JVM (Virtual Machine): The 'engine' that executes the compiled Java bytecode.");
             }
        }
    }

    public static void main(String[] args) {
        cli.MenuHandler menu = new cli.MenuHandler();
        menu.start();
    }
}

