# Twitter-Like-Api

## Table of Contents

* **[Building and Running Docker App](#script-documentation-build-and-run)**
* **[Test Cases](#running-integration-tests-with-gradle)**
* **[API Documentation](#authentication-endpoints)**

## Script Documentation-build-and-run

This script automates the process of building, deploying, and running a Dockerized application resembling a Twitter-like platform.

**Functionality:**

1. **Gradle Build:**
    * Executes the `./gradlew build` command to build the application using Gradle.
    * Checks the exit code (`$?`) to ensure the build was successful (exit code 0).
    * Exits with an error message if the build fails (exit code non-zero).

2. **Docker Image Build:**
    * Builds a Docker image for the application using the `docker build -t twitter-analog .` command.
        * `-t`: Tag the image with the name `twitter-analog`.
        * `.`: Build the image from the current directory (where the Dockerfile resides).
    * Checks the exit code (`$?`) to ensure the image build was successful.
    * Exits with an error message if the image build fails.

3. **Run Docker Compose:**
    * Starts the application using Docker Compose with the `docker-compose up -d` command.
        * `-d`: Runs the containers in detached mode (background).
    * Checks the exit code (`$?`) to ensure Docker Compose ran successfully.
    * Exits with an error message if Docker Compose fails.

4. **Success Message:**
    * If all steps are successful, the script displays a message indicating "Application deployed successfully."

**How to Use:**

1.  Make the script executable: `chmod +x build-and-run.sh`
2.  Run the script: `./build-and-run.sh`

**Assumptions:**

* The script assumes a Gradle build is configured for the project.
* A Dockerfile named `Dockerfile` is present in the current directory.
* A docker-compose.yml file is present in the current directory to define the application's services.

**Error Handling:**

* The script exits with an error message if any of the build or deployment steps fail.

## Running Integration Tests with Gradle

This document describes how to execute the integration tests for the project using Gradle. These tests are located in the `src/test/integration` directory and utilize the Spock framework.

### Prerequisites

* Java 17 (or a compatible version) must be installed on your system. You can verify your Java version by running `java -version` in your terminal.
* Gradle must be installed and configured on your system.

### Running Tests

**On Windows:**

1. Open a command prompt and navigate to the root directory of your project.
2. Execute the following command to run the integration tests:

   ```bash
   ./gradlew test
   ```

**On Linux/macOS:**

1. Open a terminal and navigate to the root directory of your project.
2. Execute the following command to run the integration tests:

   ```bash
   ./gradlew test
   ```

**Explanation:**

* `./gradlew`: This instructs Gradle to use the local Gradle wrapper script (./gradlew) to execute the task.
* `test`: This specifies the Gradle task to be executed, which is running the integration tests.

**Additional Notes:**

* The Gradle build will automatically download any necessary dependencies before executing the tests.
* The test results will be displayed in the console, indicating successful or failed tests along with detailed information.
* You can modify the Gradle task to run specific test classes or methods by using the `--tests` option followed by a test class or method name. For example, to run only the `AuthenticationControllerSpec` class:

   ```bash
   ./gradlew test --tests com.example.yourproject.integration.AuthenticationControllerSpec
   ```

By following these steps, you can effectively run and analyze the integration tests for your project on both Windows and Linux/macOS environments.


## Authentication Endpoints

Describes the authentication-related API endpoints exposed by the AuthenticationController. All endpoints are located under the base path `/api/v1/auth`.

### Endpoints

**1. Register User (POST /register):**

* Registers a new user on the platform.
    * Parameters:
        * `request` (UserRegistrationRequest): 
      ```json
        {
            "username": "",
            "password":"", 
            "firstname": "",
            "lastname": ""
        }
      ```
    * Response:
        * Response:
        * `201 CREATED ` on Success
        * `422 UNPROCESSABLE_ENTITY` on attempt to create account with existing username
        * `500 INTERNAL SERVER ERROR` on server failure.

**2. Login (POST /login):**

* Allows a registered user to log in and obtain an authentication token.
    * Parameters:
        * `request` (AuthenticationRequest): JSON object containing the user's login credentials. (AuthenticationRequest definition required)
    * Response:
        * `20 OK ` on authenticated 
        * `500 INTERNAL SERVER ERROR` on server failure.


## UserController Endpoints Version 1:

Outlines the functionalities provided by the `UserController` class. All endpoints are located under the base path `/api/v1/users`.

**Dependencies:**

* `UserService`: Injected dependency providing methods for user management operations.
* `PostService`: Injected dependency providing methods for post management operations.

**Authentication:**

* All endpoints require user authentication through the `Authorization` header.

**Endpoints:**

**1. getUserFeed(userId, page, size, authorizationHeader):**

* **Method:** GET
* **Path:** `{userId}/feed`
* **Description:** Retrieves a paginated feed of posts for the specified user.
* **Parameters:**
    * `userId` (String): Path variable representing the user ID.
    * `page` (int, optional, default: 1): Page number for pagination.
    * `size` (int, optional, default: 25): Number of items per page.
    * `authorizationHeader` (String): Authorization header containing the user's authentication token.
* **Returns:** `200 OK ` on Success or `500 INTERNAL SERVER ERROR` on failure.

**2. getPostsLikedByUser(userId, page, size, authorizationHeader):**

* **Method:** GET
* **Path:** `{userId}/posts/liked`
* **Description:** Retrieves a paginated list of posts liked by the specified user.
* **Parameters:** (Same as `getUserFeed`)
* **Returns:** `200 OK ` on Success or `500 INTERNAL SERVER ERROR` on failure.

**3. getCommentsByUser(userId, page, size, authorizationHeader):**

* **Method:** GET
* **Path:** `{userId}/comments`
* **Description:** Retrieves a paginated list of comments made by the specified user.
* **Parameters:** (Same as `getUserFeed`)
* **Returns:** `200 OK ` on Success or `500 INTERNAL SERVER ERROR` on failure.

**4. updateUser(request, userId, authorizationHeader):**

* **Method:** PATCH
* **Path:** `/` (no path variable)
* **Description:** Updates the user profile based on the provided information.
* **Parameters:**
    * `request` (UpdateUserDto): DTO object containing the update details for the user profile. (**Note:** Define `UpdateUserDto` class separately)
    * `userId` (String): Path variable representing the user ID to be updated.
    * `authorizationHeader` (String): Authorization header containing the user's authentication token.
* **Returns:** `200 OK ` on Success or `500 INTERNAL SERVER ERROR` on failure.

**5. deleteUser(userId, authorizationHeader):**

* **Method:** DELETE
* **Path:** `{userId}`
* **Description:** Deletes the specified user.
* **Parameters:**
    * `userId` (String): Path variable representing the user ID to be deleted.
    * `authorizationHeader` (String): Authorization header containing the user's authentication token.
* **Returns:** `200 OK ` on Success or `500 INTERNAL SERVER ERROR` on failure.

**6. followUser(userId, targetUser, authorizationHeader):**

* **Method:** GET
* **Path:** `{userId}/follow/{targetUser}`
* **Description:** Allows the user to follow another user.
* **Parameters:**
    * `userId` (String): Path variable representing the following user's ID.
    * `targetUser` (String): Path variable representing the user to be followed.
    * `authorizationHeader` (String): Authorization header containing the user's authentication token.
* **Returns:** `200 OK ` on Success or `500 INTERNAL SERVER ERROR` on failure.

**7. unFollowUser(userId, targetUser, authorizationHeader):**

* **Method:** GET
* **Path:** `{userId}/unfollow/{targetUser}`
* **Description:** Allows the user to unfollow another user.
* **Parameters:** (Same as `followUser`)
* **Returns:** `200 OK ` on Success or `500 INTERNAL SERVER ERROR` on failure.


## Post Controller Endpoints (version 1)

Describes the post-related API endpoints exposed by the PostController. All endpoints are located under the base path `/api/v1/posts`.

**Authentication:**

* All endpoints require user authentication through the `Authorization` header.

**Data format:**

* Request and response bodies follow JSON format.

**Error responses:**

* Specific error messages are returned for invalid requests or unauthorized access.

### Endpoints

**1. Create Post (POST):**

* Creates a new post.
    * Parameters:
        * `dto` (CreateOrEditPostDto): ```{ content: string }```
        * `userId` (String) in request body: ID of the user creating the post.
    * Response:
        *  `201 CREATED ` on Success or `500 INTERNAL SERVER ERROR` on failure.
        * `ResponseEntity<ResponseBody>` containing the newly created post information or an error message.

**2. Update Post (PUT):**

* Updates an existing post.
    * Parameters:
        * `dto` (CreateOrEditPostDto): ```{ content: string }```
        * `postId` (String): Path variable representing the ID of the post to be updated.
    * Response:
        *  `200 OK ` on Success or `500 INTERNAL SERVER ERROR` on failure.
        * `ResponseEntity<ResponseBody>` containing the updated post information or an error message.

**3. Delete Post (DELETE):**

* Deletes an existing post.
    * Parameters:
        * `postId` (String): Path variable representing the ID of the post to be deleted.
    * Response:
        * `200 OK ` on Success or `500 INTERNAL SERVER ERROR` on failure.

**4. Favorite Post (GET /{postId}/favorite/{userId}):**

* Allows a user to favorite a post.
    * Parameters:
        * `postId` (String): Path variable representing the ID of the post to favorite.
        * `userId` (String): Path variable representing the ID of the user marking post as favorite.
    * Response:
        * `200 OK ` on Success or `500 INTERNAL SERVER ERROR` on failure.

**5. Un-favorite Post (DELETE /{postId}/favorite/{userId}):**

* Allows a user to un-favorite a post.
    * Parameters: (same as Favorite Post)
    * Response:
        * `200 OK ` on Success or `500 INTERNAL SERVER ERROR` on failure.

**6. Like Post (GET /{postId}/like/{userId}):**

* Allows a user to like a post.
    * Parameters:
        * `postId` (String): Path variable representing the ID of the post to like.
        * `userId` (String): Path variable representing the ID of the user liking the post.
    * Response:
        * `200 OK ` on Success or `500 INTERNAL SERVER ERROR` on failure.

**7. Unlike Post (GET /{postId}/unlike/{userId}):**

* Allows a user to unlike a post.
    * Parameters: (same as Like Post)
    * Response:
        * `200 OK ` on Success or `500 INTERNAL SERVER ERROR` on failure.

**8. Get Post Comments (GET /{postId}/comments):**

* Retrieves all comments for a specific post.
    * Parameters:
        * `postId` (String): Path variable representing the ID of the post.
    * Response:
        * `200 OK ` on Success or `500 INTERNAL SERVER ERROR` on failure.

**9. Add Comment to Post (POST /{postId}/comments/{userId}):**

* Creates a new comment for a post.
    * Parameters:
        * `postId` (String): Path variable representing the ID of the post to comment on.
        * `userId` (String): Path variable representing the ID of the user creating the comment.
        * `dto` (CreateCommentDto): JSON object containing the comment details. (CreateCommentDto definition required)
    * Response:
        * `200 OK ` on Success or `500 INTERNAL SERVER ERROR` on failure.
