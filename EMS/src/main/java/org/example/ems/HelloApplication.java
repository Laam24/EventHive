package org.example.ems;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class HelloApplication extends Application {

    private Scene loginScene;
    private Scene signUpScene;
    private Scene homepageScene;
    private Scene hostsScene;
    private Scene addEventScene;
    private Scene eventsScene;

    // Form fields for event creation
    private TextField eventNameField;
    private TextArea eventDescriptionArea;
    private DatePicker eventDatePicker;
    private ComboBox<String> hostComboBox;
    private ComboBox<String> eventCategoryComboBox;
    private Label bannerFileLabel;
    private RadioButton publicEventRadio;
    private RadioButton privateEventRadio;
    private TextField totalAttendeesField;
    private CheckBox photographyCheckBox;
    private DatePicker registrationStartDate;
    private DatePicker registrationEndDate;
    private TextField ticketPriceField;
    private TextField maxAttendeesField;
    private HBox timePickerBox;

    @Override
    public void start(Stage stage) {
        loginScene = createLoginScene(stage);
        signUpScene = createSignUpScene(stage);
        homepageScene = createHomepageScene(stage);
        hostsScene = createHostsScene(stage);
        addEventScene = createAddEventScene(stage);
        eventsScene = createEventsScene(stage);

        stage.setTitle("EventHive");
        stage.setScene(loginScene);
        stage.show();
    }

    private Scene createLoginScene(Stage stage) {
        HBox loginWrapper = new HBox();
        loginWrapper.getStyleClass().add("login-wrapper");

        ImageView sideImage = new ImageView(new Image(getClass().getResourceAsStream("/login.png")));
        sideImage.setFitWidth(400);
        sideImage.setFitHeight(600);
        sideImage.getStyleClass().add("image-side");

        VBox formSide = new VBox(20);
        formSide.getStyleClass().add("form-side");
        formSide.setAlignment(Pos.CENTER);
        formSide.setPadding(new Insets(40));

        ImageView logo = new ImageView(new Image(getClass().getResourceAsStream("/E.png")));
        logo.getStyleClass().add("logo");

        Label titleLabel = new Label("Welcome back");
        titleLabel.getStyleClass().add("title");

        TextField emailField = createStyledTextField("Email address");
        PasswordField passwordField = createStyledPasswordField("Password");

        Hyperlink forgotPassword = new Hyperlink("Forgot password?");
        forgotPassword.getStyleClass().add("forgot-password");
        forgotPassword.setOnAction(e -> handleForgotPassword());

        Button enterResetTokenButton = createStyledButton("Enter Reset Token", "reset-token-btn");
        enterResetTokenButton.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Enter Reset Token");
            dialog.setHeaderText("Enter the password reset token you received:");
            dialog.setContentText("Token:");

            Optional<String> result = dialog.showAndWait();
            result.ifPresent(token -> showResetPasswordForm(token));
        });

        Button loginBtn = createStyledButton("Sign In", "login-btn");
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setOnAction(e -> {
            String username = emailField.getText().trim();
            String password = passwordField.getText();

            if (username.isEmpty() || password.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Login Failed", "Username and password are required.");
                return;
            }

            if (DatabaseUtil.validateLogin(username, password)) {
                stage.setScene(homepageScene);
            } else {
                showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid username or password. Please try again.");
            }
        });

        Hyperlink signupLink = new Hyperlink("Sign up");
        signupLink.getStyleClass().add("signup-link");
        signupLink.setOnAction(e -> stage.setScene(signUpScene));

        Label signupText = new Label("Don't have an account? ");
        signupText.getStyleClass().add("signup");
        HBox signupBox = new HBox(signupText, signupLink);
        signupBox.setAlignment(Pos.CENTER);

        formSide.getChildren().addAll(logo, titleLabel, emailField, passwordField, forgotPassword,
                loginBtn, signupBox);

        loginWrapper.getChildren().addAll(sideImage, formSide);

        Scene scene = new Scene(loginWrapper, 800, 600);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        return scene;
    }

    private void handleForgotPassword() {
        Stage forgotPasswordStage = new Stage();
        forgotPasswordStage.initModality(Modality.APPLICATION_MODAL);
        forgotPasswordStage.setTitle("Password Recovery");

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("Password Recovery");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        TextField emailField = createStyledTextField("Enter your email");
        Button submitButton = createStyledButton("Submit", "submit-btn");
        Label messageLabel = new Label();
        messageLabel.setWrapText(true);

        submitButton.setOnAction(e -> {
            String email = emailField.getText().trim();
            if (email.isEmpty()) {
                messageLabel.setText("Please enter your email.");
                messageLabel.setTextFill(javafx.scene.paint.Color.RED);
            } else {
                if (DatabaseUtil.emailExists(email)) {
                    String resetToken = PasswordRecoveryService.generateResetToken();
                    if (DatabaseUtil.storeResetToken(email, resetToken)) {
                        PasswordRecoveryService.sendResetEmail(email, resetToken);
                        messageLabel.setText("A password reset link has been sent to your email. Please check your inbox.");
                        messageLabel.setTextFill(javafx.scene.paint.Color.GREEN);
                        submitButton.setDisable(true);
                    } else {
                        messageLabel.setText("An error occurred. Please try again later.");
                        messageLabel.setTextFill(javafx.scene.paint.Color.RED);
                    }
                } else {
                    messageLabel.setText("If an account exists for this email, you will receive password reset instructions.");
                    messageLabel.setTextFill(javafx.scene.paint.Color.GREEN);
                }
            }
        });

        layout.getChildren().addAll(titleLabel, emailField, submitButton, messageLabel);

        Scene scene = new Scene(layout, 400, 250);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        forgotPasswordStage.setScene(scene);
        forgotPasswordStage.showAndWait();
    }

    private void showResetPasswordForm(String token) {
        Stage resetStage = new Stage();
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);

        PasswordField newPasswordField = createStyledPasswordField("New Password");
        PasswordField confirmPasswordField = createStyledPasswordField("Confirm New Password");
        Button resetButton = createStyledButton("Reset Password", "submit-btn");
        Label messageLabel = new Label();

        resetButton.setOnAction(e -> {
            String newPassword = newPasswordField.getText();
            String confirmPassword = confirmPasswordField.getText();

            if (!newPassword.equals(confirmPassword)) {
                messageLabel.setText("Passwords do not match.");
                messageLabel.setTextFill(javafx.scene.paint.Color.RED);
                return;
            }

            if (PasswordRecoveryService.resetPassword(token, newPassword)) {
                messageLabel.setText("Password reset successfully. You can now log in with your new password.");
                messageLabel.setTextFill(javafx.scene.paint.Color.GREEN);
                resetButton.setDisable(true);
            } else {
                messageLabel.setText("Failed to reset password. Please try again or request a new reset link.");
                messageLabel.setTextFill(javafx.scene.paint.Color.RED);
            }
        });

        layout.getChildren().addAll(
                new Label("Enter your new password"),
                newPasswordField,
                confirmPasswordField,
                resetButton,
                messageLabel
        );

        Scene scene = new Scene(layout, 300, 250);
        resetStage.setScene(scene);
        resetStage.setTitle("Reset Password");
        resetStage.show();
    }

    private Scene createSignUpScene(Stage stage) {
        HBox container = new HBox();
        container.getStyleClass().add("container");

        VBox imageSection = new VBox();
        imageSection.getStyleClass().add("image-section");
        imageSection.setPrefWidth(400);

        ImageView signUpImage = new ImageView(new Image(getClass().getResourceAsStream("/login.png")));
        signUpImage.setFitWidth(400);
        signUpImage.setFitHeight(600);
        signUpImage.setPreserveRatio(true);
        imageSection.getChildren().add(signUpImage);

        VBox formSection = new VBox(15);
        formSection.getStyleClass().add("form-section");
        formSection.setPadding(new Insets(40));
        formSection.setAlignment(Pos.CENTER);
        formSection.setPrefWidth(500);

        Label titleLabel = new Label("Create an Account");
        titleLabel.getStyleClass().add("title");

        TextField fnameField = createStyledTextField("First Name");
        TextField lnameField = createStyledTextField("Last Name");
        TextField usernameField = createStyledTextField("Username");
        TextField contactField = createStyledTextField("Contact no.");
        TextField emailField = createStyledTextField("Email Address");
        PasswordField passwordField = createStyledPasswordField("Password");
        PasswordField confirmPasswordField = createStyledPasswordField("Confirm Password");

        Button signUpButton = createStyledButton("Sign Up", "submit-btn");
        signUpButton.setMaxWidth(Double.MAX_VALUE);
        signUpButton.setOnAction(e -> {
            String firstName = fnameField.getText().trim();
            String lastName = lnameField.getText().trim();
            String username = usernameField.getText().trim();
            String contact = contactField.getText().trim();
            String email = emailField.getText().trim();
            String password = passwordField.getText();
            String confirmPassword = confirmPasswordField.getText();

            if (firstName.isEmpty() || lastName.isEmpty() || username.isEmpty() ||
                    contact.isEmpty() || email.isEmpty() || password.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Registration Failed", "All fields are required.");
                return;
            }

            if (!password.equals(confirmPassword)) {
                showAlert(Alert.AlertType.ERROR, "Password Mismatch", "Passwords do not match. Please try again.");
                return;
            }

            try {
                if (DatabaseUtil.registerUser(firstName, lastName, username, email, contact, password)) {
                    showAlert(Alert.AlertType.INFORMATION, "Registration Successful", "You have successfully registered!");
                    stage.setScene(loginScene);
                } else {
                    showAlert(Alert.AlertType.ERROR, "Registration Failed", "Registration failed. Please try again.");
                }
            } catch (SQLException ex) {
                showAlert(Alert.AlertType.ERROR, "Registration Failed", "An error occurred: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        Hyperlink loginLink = new Hyperlink("Return to login");
        loginLink.setOnAction(e -> stage.setScene(loginScene));

        Label loginText = new Label("Have an account? ");
        HBox loginBox = new HBox(loginText, loginLink);
        loginBox.setAlignment(Pos.CENTER);
        loginBox.getStyleClass().add("login-link");

        formSection.getChildren().addAll(
                titleLabel, fnameField, lnameField, usernameField, contactField,
                emailField, passwordField, confirmPasswordField, signUpButton, loginBox
        );

        container.getChildren().addAll(imageSection, formSection);

        Scene scene = new Scene(container, 900, 600);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        return scene;
    }

    private Scene createHomepageScene(Stage stage) {
        ScrollPane scrollPane = new ScrollPane();
        VBox root = new VBox();
        root.getStyleClass().add("root");
        root.setSpacing(0);

        HBox navbar = createNavbar(stage);

        StackPane hero = new StackPane();
        hero.getStyleClass().add("hero");
        hero.setMinHeight(600);

        String backgroundImagePath = "/concert-audience.png";
        try {
            URL imageUrl = getClass().getResource(backgroundImagePath);
            if (imageUrl == null) {
                System.err.println("Could not find image: " + backgroundImagePath);
                hero.setStyle("-fx-background-color: #f0f0f0;");
            } else {
                Image heroImage = new Image(imageUrl.toExternalForm());
                BackgroundImage bgImage = new BackgroundImage(
                        heroImage,
                        BackgroundRepeat.NO_REPEAT,
                        BackgroundRepeat.NO_REPEAT,
                        BackgroundPosition.CENTER,
                        new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, true, true)
                );
                hero.setBackground(new Background(bgImage));
            }
        } catch (Exception e) {
            System.err.println("Error loading image: " + e.getMessage());
            e.printStackTrace();
            hero.setStyle("-fx-background-color: #f0f0f0;");
        }

        VBox heroContent = new VBox(20);
        heroContent.getStyleClass().add("hero-content");
        heroContent.setMaxWidth(600);
        heroContent.setAlignment(Pos.CENTER);

        Label heroTitle = new Label("Discover Amazing Events");
        heroTitle.getStyleClass().add("hero-title");

        Label heroText = new Label("Find and book unforgettable experiences in your area");
        heroText.getStyleClass().add("hero-text");
        heroText.setWrapText(true);

        Button exploreBtn = createStyledButton("Explore Events", "cta-btn");

        heroContent.getChildren().addAll(heroTitle, heroText, exploreBtn);
        hero.getChildren().add(heroContent);

        VBox contentSections = new VBox(60);
        contentSections.setPadding(new Insets(60, 0, 60, 0));
        contentSections.getChildren().addAll(
                createPopularHostsSection(4, stage),
                createEventSection("Upcoming Events", 4, stage)
        );

        root.getChildren().addAll(navbar, hero, contentSections);
        scrollPane.setContent(root);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        Scene scene = new Scene(scrollPane, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        return scene;
    }

    private Scene createHostsScene(Stage stage) {
        ScrollPane scrollPane = new ScrollPane();
        VBox root = new VBox();
        root.getStyleClass().add("root");
        root.setSpacing(0);

        HBox navbar = createNavbar(stage);

        VBox hostsSection = new VBox(30);
        hostsSection.setPadding(new Insets(60, 50, 60, 50));

        Label title = new Label("Our Hosts");
        title.getStyleClass().add("section-title");

        FlowPane cardContainer = new FlowPane();
        cardContainer.setHgap(20);
        cardContainer.setVgap(20);
        cardContainer.getStyleClass().add("card-container");

        List<Host> hosts = DatabaseUtil.getHosts();
        System.out.println("Fetched " + hosts.size() + " hosts from database");

        for (Host host : hosts) {
            VBox hostCard = createHostCard(host);
            cardContainer.getChildren().add(hostCard);
            System.out.println("Added card for host: " + host.getName());
        }

        hostsSection.getChildren().addAll(title, cardContainer);

        root.getChildren().addAll(navbar, hostsSection);
        scrollPane.setContent(root);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        Scene scene = new Scene(scrollPane, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        return scene;
    }

    private Scene createEventsScene(Stage stage) {
        ScrollPane scrollPane = new ScrollPane();
        VBox root = new VBox();
        root.getStyleClass().add("root");
        root.setSpacing(20);

        HBox navbar = createNavbar(stage);

        VBox eventsSection = new VBox(30);
        eventsSection.setPadding(new Insets(60, 50, 60, 50));

        Label title = new Label("All Events");
        title.getStyleClass().add("section-title");

        FlowPane cardContainer = new FlowPane();
        cardContainer.setHgap(20);
        cardContainer.setVgap(20);
        cardContainer.getStyleClass().add("card-container");

        List<Event> events = DatabaseUtil.getEvents();
        for (Event event : events) {
            VBox eventCard = createEventCard(stage, event);
            cardContainer.getChildren().add(eventCard);
        }

        eventsSection.getChildren().addAll(title, cardContainer);
        root.getChildren().addAll(navbar, eventsSection);
        scrollPane.setContent(root);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        Scene scene = new Scene(scrollPane, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        return scene;
    }

    private Scene createAddEventScene(Stage stage) {
        ScrollPane scrollPane = new ScrollPane();
        VBox root = new VBox();
        root.getStyleClass().add("root");
        root.setSpacing(20);

        HBox navbar = createNavbar(stage);

        VBox formContainer = new VBox(20);
        formContainer.setPadding(new Insets(40));
        formContainer.getStyleClass().add("form-container");

        Label title = new Label("Add New Event");
        title.getStyleClass().add("section-title");

        eventNameField = createStyledTextField("Event Name");
        eventDescriptionArea = createStyledTextArea("Event Description");
        eventDatePicker = createStyledDatePicker("Event Date");

        ComboBox<String> hourComboBox = new ComboBox<>();
        for (int i = 1; i <= 12; i++) {
            hourComboBox.getItems().add(String.format("%02d", i));
        }
        hourComboBox.setPromptText("Hour");

        ComboBox<String> minuteComboBox = new ComboBox<>();
        for (int i = 0; i < 60; i += 5) {
            minuteComboBox.getItems().add(String.format("%02d", i));
        }
        minuteComboBox.setPromptText("Minute");

        ComboBox<String> amPmComboBox = new ComboBox<>();
        amPmComboBox.getItems().addAll("AM", "PM");
        amPmComboBox.setPromptText("AM/PM");

        timePickerBox = new HBox(10, hourComboBox, new Label(":"), minuteComboBox, amPmComboBox);
        timePickerBox.setAlignment(Pos.CENTER_LEFT);

        hostComboBox = createStyledComboBox("Select Host");
        populateHostDropdown(hostComboBox);
        eventCategoryComboBox = createStyledComboBox("Event Category",
                "Birthday", "Wedding", "Concert", "Seminar", "Other");

        HBox bannerBox = new HBox(10);
        bannerBox.setAlignment(Pos.CENTER_LEFT);
        Button uploadBannerBtn = createStyledButton("Choose File", "upload-btn");
        bannerFileLabel = new Label("No file chosen");
        bannerFileLabel.getStyleClass().add("file-label");
        bannerBox.getChildren().addAll(uploadBannerBtn, bannerFileLabel);

        uploadBannerBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Choose Event Banner");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
            );
            File selectedFile = fileChooser.showOpenDialog(stage);
            if (selectedFile != null) {
                bannerFileLabel.setText(selectedFile.getAbsolutePath());
            }
        });

        Label eventTypeLabel = new Label("Event Type");
        eventTypeLabel.getStyleClass().add("form-label");
        ToggleGroup eventTypeGroup = new ToggleGroup();
        publicEventRadio = new RadioButton("Public Event");
        privateEventRadio = new RadioButton("Private Event");
        publicEventRadio.setToggleGroup(eventTypeGroup);
        privateEventRadio.setToggleGroup(eventTypeGroup);
        HBox eventTypeBox = new HBox(20, publicEventRadio, privateEventRadio);

        VBox privateEventOptions = new VBox(10);
        privateEventOptions.setVisible(false);
        totalAttendeesField = createStyledTextField("Total Number of Attendees");
        photographyCheckBox = new CheckBox("Provide photography services");
        privateEventOptions.getChildren().addAll(totalAttendeesField, photographyCheckBox);

        VBox publicEventOptions = new VBox(10);
        publicEventOptions.setVisible(false);
        registrationStartDate = createStyledDatePicker("Registration Start Date");
        registrationEndDate = createStyledDatePicker("Registration End Date");
        ticketPriceField = createStyledTextField("Ticket Price");
        maxAttendeesField = createStyledTextField("Maximum Number of Attendees");
        publicEventOptions.getChildren().addAll(registrationStartDate, registrationEndDate,
                ticketPriceField, maxAttendeesField);

        eventTypeGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            privateEventOptions.setVisible(newValue == privateEventRadio);
            publicEventOptions.setVisible(newValue == publicEventRadio);
        });

        Button submitButton = createStyledButton("Create Event", "submit-btn");
        submitButton.setOnAction(e -> {
            if (validateEventForm()) {
                Event newEvent = createEventFromForm();
                if (newEvent != null) {
                    System.out.println("Attempting to insert event: " + newEvent);
                    boolean insertSuccess = DatabaseUtil.insertEvent(newEvent);
                    System.out.println("Insert result: " + insertSuccess);
                    if (insertSuccess) {
                        showAlert(Alert.AlertType.INFORMATION, "Event Created", "Your event has been successfully created!");
                        eventsScene = createEventsScene(stage);
                        stage.setScene(eventsScene);
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to insert the event into the database. Please check the database connection and try again.");
                    }
                } else {
                    showAlert(Alert.AlertType.ERROR, "Form Error", "Failed to create event from form data. Please check your inputs and try again.");
                }
            }
        });

        formContainer.getChildren().addAll(title, eventNameField, eventDescriptionArea, eventDatePicker,
                new Label("Event Time:"), timePickerBox, hostComboBox, eventCategoryComboBox,
                new Label("Event Banner"), bannerBox,
                eventTypeLabel, eventTypeBox, privateEventOptions, publicEventOptions, submitButton);

        root.getChildren().addAll(navbar, formContainer);
        scrollPane.setContent(root);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        Scene scene = new Scene(scrollPane, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        return scene;
    }

    private Scene createContactScene(Stage stage) {
        ScrollPane scrollPane = new ScrollPane();
        VBox root = new VBox(0);
        root.getStyleClass().add("root");

        HBox navbar = createNavbar(stage);

        VBox contentContainer = new VBox(30);
        contentContainer.getStyleClass().add("content-container");
        contentContainer.setPadding(new Insets(60, 50, 60, 50));
        contentContainer.setAlignment(Pos.TOP_CENTER);

        Label title = new Label("Contact Us");
        title.getStyleClass().add("section-title");

        VBox contactInfo = new VBox(20);
        contactInfo.setAlignment(Pos.CENTER);
        contactInfo.getStyleClass().add("contact-info");

        Label companyName = new Label("EventHive Inc.");
        companyName.getStyleClass().add("company-name");

        Label addressLabel = new Label("123 Event Street, Suite 456\nEventCity, EC 12345");
        addressLabel.getStyleClass().add("contact-detail");

        Label phoneLabel = new Label("Phone: +1 (555) 123-4567");
        phoneLabel.getStyleClass().add("contact-detail");

        Label emailLabel = new Label("Email: support@eventhive.com");
        emailLabel.getStyleClass().add("contact-detail");

        Label hours = new Label("Business Hours: Monday - Friday, 9AM - 5PM EST");
        hours.getStyleClass().add("contact-detail");

        TextField nameField = createStyledTextField("Your Name");
        TextField emailField = createStyledTextField("Your Email");

        TextArea messageArea = new TextArea();
        messageArea.setPromptText("Send us a message");
        messageArea.setPrefRowCount(5);
        messageArea.getStyleClass().add("styled-text-area");

        Button sendButton = createStyledButton("Send Message", "submit-btn");
        sendButton.setOnAction(e -> {
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            String message = messageArea.getText().trim();

            if (name.isEmpty() || email.isEmpty() || message.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Error", "Please fill in all fields.");
                return;
            }

            if (DatabaseUtil.insertContactMessage(name, email, message)) {
                showAlert(Alert.AlertType.INFORMATION, "Message Sent", "Thank you for your message. We'll get back to you soon!");
                nameField.clear();
                emailField.clear();
                messageArea.clear();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to send message. Please try again later.");
            }
        });

        contactInfo.getChildren().addAll(companyName, addressLabel, phoneLabel, emailLabel, hours,
                nameField, emailField, messageArea, sendButton);

        contentContainer.getChildren().addAll(title, contactInfo);

        root.getChildren().addAll(navbar, contentContainer);
        scrollPane.setContent(root);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        Scene scene = new Scene(scrollPane, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        return scene;
    }

    private HBox createNavbar(Stage stage) {
        HBox navbar = new HBox();
        navbar.getStyleClass().add("navbar");
        navbar.setPadding(new Insets(20, 50, 20, 50));
        navbar.setAlignment(Pos.CENTER_LEFT);

        Label logo = new Label("EventHive");
        logo.getStyleClass().add("logo");

        HBox navLinks = new HBox(20);
        navLinks.setAlignment(Pos.CENTER);
        String[] navItems = {"Home", "Hosts", "Events", "Add Events", "About", "Contact"};
        for (String item : navItems) {
            Button navButton = createStyledButton(item, "nav-button");
            if (item.equals("Home")) {
                navButton.setOnAction(e -> stage.setScene(homepageScene));
            } else if (item.equals("Hosts")) {
                navButton.setOnAction(e -> stage.setScene(hostsScene));
            } else if (item.equals("Events")) {
                navButton.setOnAction(e -> stage.setScene(createEventsScene(stage)));
            } else if (item.equals("Add Events")) {
                navButton.setOnAction(e -> stage.setScene(addEventScene));
            } else if (item.equals("Contact")) {
                navButton.setOnAction(e -> stage.setScene(createContactScene(stage)));
            }
            navLinks.getChildren().add(navButton);
        }

        Button loginBtn = createStyledButton("Log Out", "login-btn");
        loginBtn.setOnAction(e -> stage.setScene(loginScene));

        navbar.getChildren().addAll(logo, navLinks, loginBtn);
        HBox.setHgrow(navLinks, Priority.ALWAYS);

        return navbar;
    }

    private void populateHostDropdown(ComboBox<String> hostComboBox) {
        List<Host> hosts = DatabaseUtil.getHosts();
        hostComboBox.getItems().clear();
        for (Host host : hosts) {
            hostComboBox.getItems().add(host.getId() + " - " + host.getName());
        }
    }

    private boolean validateEventForm() {
        ComboBox<String> hourComboBox = (ComboBox<String>) timePickerBox.getChildren().get(0);
        ComboBox<String> minuteComboBox = (ComboBox<String>) timePickerBox.getChildren().get(2);
        ComboBox<String> amPmComboBox = (ComboBox<String>) timePickerBox.getChildren().get(3);

        if (eventNameField.getText().isEmpty() ||
                eventDescriptionArea.getText().isEmpty() ||
                eventDatePicker.getValue() == null ||
                hourComboBox.getValue() == null ||
                minuteComboBox.getValue() == null ||
                amPmComboBox.getValue() == null ||
                hostComboBox.getValue() == null ||
                eventCategoryComboBox.getValue() == null ||
                bannerFileLabel.getText().equals("No file chosen") ||
                (publicEventRadio.isSelected() && (
                        registrationStartDate.getValue() == null ||
                                registrationEndDate.getValue() == null ||
                                ticketPriceField.getText().isEmpty() ||
                                maxAttendeesField.getText().isEmpty()
                )) ||
                (privateEventRadio.isSelected() && totalAttendeesField.getText().isEmpty())) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please fill in all required fields.");
            return false;
        }
        return true;
    }

    private Event createEventFromForm() {
        try {
            String hostValue = hostComboBox.getValue();
            int hostId = Integer.parseInt(hostValue.split(" - ")[0]);

            String hour = ((ComboBox<String>) timePickerBox.getChildren().get(0)).getValue();
            String minute = ((ComboBox<String>) timePickerBox.getChildren().get(2)).getValue();
            String amPm = ((ComboBox<String>) timePickerBox.getChildren().get(3)).getValue();

            if (hour == null || minute == null || amPm == null) {
                throw new IllegalArgumentException("Please select hour, minute, and AM/PM for the event time.");
            }

            int hourInt = Integer.parseInt(hour);
            if (amPm.equals("PM") && hourInt != 12) {
                hourInt += 12;
            } else if (amPm.equals("AM") && hourInt == 12) {
                hourInt = 0;
            }

            LocalTime eventTime = LocalTime.of(hourInt, Integer.parseInt(minute));

            Event newEvent = new Event(
                    eventNameField.getText(),
                    eventDescriptionArea.getText(),
                    eventDatePicker.getValue(),
                    eventTime,
                    hostId,
                    eventCategoryComboBox.getValue(),
                    bannerFileLabel.getText(),
                    publicEventRadio.isSelected() ? "public" : "private"
            );

            if (publicEventRadio.isSelected()) {
                newEvent.setRegistrationStart(registrationStartDate.getValue());
                newEvent.setRegistrationEnd(registrationEndDate.getValue());
                newEvent.setTicketPrice(Double.parseDouble(ticketPriceField.getText()));
                newEvent.setMaxAttendees(Integer.parseInt(maxAttendeesField.getText()));
            } else {
                newEvent.setTotalAttendees(Integer.parseInt(totalAttendeesField.getText()));
                newEvent.setPhotographyService(photographyCheckBox.isSelected());
            }
            return newEvent;
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to create event: " + e.getMessage());
            return null;
        }
    }

    private VBox createPopularHostsSection(int cardCount, Stage stage) {
        VBox section = new VBox(30);
        section.setPadding(new Insets(0, 50, 0, 50));

        Label title = new Label("Popular Hosts");
        title.getStyleClass().add("section-title");

        FlowPane cardContainer = new FlowPane();
        cardContainer.setHgap(20);
        cardContainer.setVgap(20);
        cardContainer.getStyleClass().add("card-container");

        List<Host> popularHosts = getPopularHosts(cardCount);
        for (Host host : popularHosts) {
            cardContainer.getChildren().add(createHostCardForHomepage(host, stage));
        }

        section.getChildren().addAll(title, cardContainer);
        return section;
    }

    private VBox createEventSection(String sectionTitle, int cardCount, Stage stage) {
        VBox section = new VBox(30);
        section.setPadding(new Insets(0, 50, 0, 50));

        Label title = new Label(sectionTitle);
        title.getStyleClass().add("section-title");

        FlowPane cardContainer = new FlowPane();
        cardContainer.setHgap(20);
        cardContainer.setVgap(20);
        cardContainer.getStyleClass().add("card-container");

        List<Event> events = DatabaseUtil.getEvents();
        for (int i = 0; i < Math.min(cardCount, events.size()); i++) {
            cardContainer.getChildren().add(createEventCard(stage, events.get(i)));
        }

        section.getChildren().addAll(title, cardContainer);
        return section;
    }

    private VBox createHostCardForHomepage(Host host, Stage stage) {
        VBox card = new VBox(15);
        card.getStyleClass().add("host-card");

        ImageView hostImage = new ImageView(new Image(host.getImageUrl(), 300, 200, true, true));
        hostImage.setFitWidth(300);
        hostImage.setFitHeight(200);
        hostImage.getStyleClass().add("host-image");

        Label nameLabel = new Label(host.getName());
        nameLabel.getStyleClass().add("host-name");

        Label specialtyLabel = new Label("Specialty: " + host.getSpecialty());
        specialtyLabel.getStyleClass().add("host-specialty");

        Label ratingLabel = new Label(String.format("Rating: %.1f/5.0", host.getRating()));
        ratingLabel.getStyleClass().add("host-rating");

        Button viewMoreBtn = createStyledButton("View More", "view-more-btn");
        viewMoreBtn.setOnAction(e -> showHostDetailsPage(host));

        card.getChildren().addAll(hostImage, nameLabel, specialtyLabel, ratingLabel, viewMoreBtn);

        return card;
    }

    private VBox createHostCard(Host host) {
        VBox card = new VBox(15);
        card.getStyleClass().add("host-card");

        ImageView hostImage = new ImageView(new Image(host.getImageUrl(), true));
        hostImage.setFitWidth(300);
        hostImage.setFitHeight(200);
        hostImage.getStyleClass().add("host-image");

        Label nameLabel = new Label(host.getName());
        nameLabel.getStyleClass().add("host-name");

        Label specialtyLabel = new Label("Specialty: " + host.getSpecialty());
        specialtyLabel.getStyleClass().add("host-specialty");

        Label ratingLabel = new Label(String.format("Rating: %.1f/5.0", host.getRating()));
        ratingLabel.getStyleClass().add("host-rating");

        Button viewMoreBtn = createStyledButton("View More", "view-more-btn");
        viewMoreBtn.setOnAction(e -> showHostDetailsPage(host));

        card.getChildren().addAll(hostImage, nameLabel, specialtyLabel, ratingLabel, viewMoreBtn);

        return card;
    }

    private void showHostDetailsPage(Host host) {
        Stage detailStage = new Stage();
        ScrollPane scrollPane = new ScrollPane();
        VBox root = new VBox(20);
        root.getStyleClass().add("root");
        root.setPadding(new Insets(20));

        VBox detailsContainer = new VBox(20);
        detailsContainer.getStyleClass().add("host-details-container");
        detailsContainer.setPadding(new Insets(20));

        ImageView hostImage = new ImageView(new Image(host.getImageUrl(), true));
        hostImage.setFitWidth(400);
        hostImage.setFitHeight(300);
        hostImage.getStyleClass().add("host-detail-image");

        Label nameLabel = new Label(host.getName());
        nameLabel.getStyleClass().add("host-detail-name");

        Label specialtyLabel = new Label("Specialty: " + host.getSpecialty());
        specialtyLabel.getStyleClass().add("host-detail-info");

        Label ratingLabel = new Label(String.format("Rating: %.1f/5.0", host.getRating()));
        ratingLabel.getStyleClass().add("host-detail-info");

        Label descriptionLabel = new Label("Description: " + host.getDescription());
        descriptionLabel.getStyleClass().add("host-detail-description");
        descriptionLabel.setWrapText(true);

        Button contactButton = createStyledButton("Contact Host", "cta-btn");
        contactButton.setOnAction(e -> {
            showAlert(Alert.AlertType.INFORMATION, "Contact", "Contact functionality to be implemented.");
        });

        Button backButton = createStyledButton("Back", "back-btn");
        backButton.setOnAction(e -> detailStage.close());

        HBox buttonBox = new HBox(20, contactButton, backButton);
        buttonBox.setAlignment(Pos.CENTER);

        detailsContainer.getChildren().addAll(hostImage, nameLabel, specialtyLabel, ratingLabel,
                descriptionLabel, buttonBox);

        root.getChildren().addAll(detailsContainer);
        scrollPane.setContent(root);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        Scene detailsScene = new Scene(scrollPane, 800, 600);
        detailsScene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        detailStage.setScene(detailsScene);
        detailStage.setTitle("Host Details: " + host.getName());
        detailStage.show();
    }

    private VBox createEventCard(Stage stage, Event event) {
        VBox card = new VBox(15);
        card.getStyleClass().add("event-card");

        ImageView eventImage;
        try {
            File file = new File(event.getBannerPath());
            String localUrl = file.toURI().toURL().toString();
            Image image = new Image(localUrl, 300, 200, true, true);
            eventImage = new ImageView(image);
        } catch (MalformedURLException e) {
            System.err.println("Error loading image: " + e.getMessage());
            eventImage = new ImageView(new Image(getClass().getResourceAsStream("/placeholder.png")));
        }
        eventImage.setFitWidth(300);
        eventImage.setFitHeight(200);
        eventImage.getStyleClass().add("event-image");

        Label nameLabel = new Label(event.getName());
        nameLabel.getStyleClass().add("event-name");

        Label dateLabel = new Label(event.getEventDate().toString());
        dateLabel.getStyleClass().add("event-date");

        Label categoryLabel = new Label(event.getCategory());
        categoryLabel.getStyleClass().add("event-category");

        Button viewMoreBtn = createStyledButton("View More", "view-more-btn");
        viewMoreBtn.setOnAction(e -> showEventDetailsPage(stage, event));

        card.getChildren().addAll(eventImage, nameLabel, dateLabel, categoryLabel, viewMoreBtn);

        return card;
    }

    private void showEventDetailsPage(Stage stage, Event event) {
        ScrollPane scrollPane = new ScrollPane();
        VBox root = new VBox(20);
        root.getStyleClass().add("root");
        root.setPadding(new Insets(20));

        HBox navbar = createNavbar(stage);

        VBox detailsContainer = new VBox(20);
        detailsContainer.getStyleClass().add("event-details-container");
        detailsContainer.setPadding(new Insets(20));

        ImageView eventImage;
        try {
            File file = new File(event.getBannerPath());
            String localUrl = file.toURI().toURL().toString();
            Image image = new Image(localUrl, 600, 400, true, true);
            eventImage = new ImageView(image);
        } catch (MalformedURLException e) {
            System.err.println("Error loading image: " + e.getMessage());
            eventImage = new ImageView(new Image(getClass().getResourceAsStream("/placeholder.png")));
        }
        eventImage.setFitWidth(600);
        eventImage.setFitHeight(400);
        eventImage.getStyleClass().add("event-detail-image");

        Label nameLabel = new Label(event.getName());
        nameLabel.getStyleClass().add("event-detail-name");

        Label dateLabel = new Label("Date: " + event.getEventDate().toString());
        dateLabel.getStyleClass().add("event-detail-info");

        Label timeLabel = new Label("Time: " + event.getEventTime().toString());
        timeLabel.getStyleClass().add("event-detail-info");

        Label categoryLabel = new Label("Category: " + event.getCategory());
        categoryLabel.getStyleClass().add("event-detail-info");

        Label descriptionLabel = new Label("Description: " + event.getDescription());
        descriptionLabel.getStyleClass().add("event-detail-description");
        descriptionLabel.setWrapText(true);

        Button bookButton = createStyledButton("Book Now", "cta-btn");
        bookButton.setOnAction(e -> {
            showAlert(Alert.AlertType.INFORMATION, "Booking", "Booking functionality to be implemented.");
        });

        Button backButton = createStyledButton("Back", "back-btn");
        backButton.setOnAction(e -> stage.setScene(eventsScene));

        HBox buttonBox = new HBox(20, bookButton, backButton);
        buttonBox.setAlignment(Pos.CENTER);

        detailsContainer.getChildren().addAll(eventImage, nameLabel, dateLabel, timeLabel,
                categoryLabel, descriptionLabel, buttonBox);

        root.getChildren().addAll(navbar, detailsContainer);
        scrollPane.setContent(root);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        Scene detailsScene = new Scene(scrollPane, 1200, 800);
        detailsScene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        stage.setScene(detailsScene);
    }

    private List<Host> getPopularHosts(int limit) {
        List<Host> allHosts = DatabaseUtil.getHosts();
        allHosts.sort((h1, h2) -> Double.compare(h2.getRating(), h1.getRating()));
        return allHosts.stream().limit(limit).collect(Collectors.toList());
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private TextArea createStyledTextArea(String promptText) {
        TextArea textArea = new TextArea();
        textArea.setPromptText(promptText);
        textArea.setPrefWidth(300);
        textArea.setPrefHeight(100);
        textArea.getStyleClass().add("styled-text-area");
        return textArea;
    }

    private DatePicker createStyledDatePicker(String promptText) {
        DatePicker datePicker = new DatePicker();
        datePicker.setPromptText(promptText);
        datePicker.setPrefWidth(300);
        datePicker.setPrefHeight(40);
        datePicker.getStyleClass().add("styled-date-picker");
        return datePicker;
    }

    private ComboBox<String> createStyledComboBox(String promptText, String... items) {
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.setPromptText(promptText);
        comboBox.getItems().addAll(items);
        comboBox.setPrefWidth(300);
        comboBox.setPrefHeight(40);
        comboBox.getStyleClass().add("styled-combo-box");
        return comboBox;
    }

    private TextField createStyledTextField(String promptText) {
        TextField textField = new TextField();
        textField.setPromptText(promptText);
        textField.setPrefWidth(300);
        textField.setPrefHeight(40);
        textField.getStyleClass().add("styled-text-field");
        return textField;
    }

    private PasswordField createStyledPasswordField(String promptText) {
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText(promptText);
        passwordField.setPrefWidth(300);
        passwordField.setPrefHeight(40);
        passwordField.getStyleClass().add("styled-text-field");
        return passwordField;
    }

    private Button createStyledButton(String text, String styleClass) {
        Button button = new Button(text);
        button.getStyleClass().addAll("styled-button", styleClass);
        return button;
    }

    public static void main(String[] args) {
        launch();
    }
}