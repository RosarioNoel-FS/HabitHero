# Habit Hero

**Habit Hero** is a mobile application designed to help users track and manage their habits effectively. It utilizes a user-friendly interface on both emulators and physical devices to ensure optimal performance and user experience.

## Application Overview

### Authentication
- Google Sign-In and email/password authentication via Firebase.

### Initial Setup
- New users set up their username upon first sign-in, leading to the main navigation.

### Main Interface
- Bottom navigation bar with Home and Rewards sections.
- Home screen prompts users to create habits if none are set.

### Habit Management
- Users can create habits via a floating action button that opens the Habit Category screen.
- Habit categories include:
  - Health & Fitness
  - Mindfulness & Wellbeing
  - Learning & Growth
  - Creativity & Expression
  - Adventure & Exploration
- Features include setting habit deadlines, tracking streaks, and managing habits through a detailed view interface.

### Settings and Data Management
- Options to modify username and sign out.
- All data is securely managed through Firebase Firestore and Realtime Database.

### Device Compatibility
- Tested on Pixel 7 (API 30-32), Samsung Ultra 22 and 23.

## GitHub Commit Summary

### Major Milestones
- **Alpha Version (Nov 19, 2023):** Foundation of the app with CRUD operations for habit management.
- **UI Overhauls (Nov 17 & Dec 19, 2023):** Major updates to the Home screen and habit management UI, enhancing usability and aesthetics.
- **Release Candidate (Dec 14, 2023):** Application achieved a bug-free state with all features implemented.

### Feature Implementation Timeline
- **Google Sign-In (Oct 26, 2023):** Integrated Google authentication.
- **Habit Tracking and Management (Nov 15-22, 2023):** Implemented custom habit creation, completion, and deletion functionalities.
- **UI and Usability Enhancements (Dec 1-10, 2023):** Added informative modals and sound feedback for a better user experience.
- **Stability and Data Management Fixes (Nov 26 - Dec 2, 2023):** Resolved issues related to profile image handling and habit data synchronization.

## Getting Started

To clone and run this application, use the following Git command:git clone https://github.com/RosarioNoel-FS/HabitHero.git

Ensure you have the latest version of Android Studio and the required SDKs installed as per the application's development environment settings.

## Feedback

Please feel free to fork the repository, make improvements, and submit pull requests. For bugs, suggestions, or additional information, please open an issue in the repository.

