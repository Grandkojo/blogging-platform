// package com.blogging_platform.service;

// import static org.junit.jupiter.api.Assertions.assertEquals;
// import static org.junit.jupiter.api.Assertions.assertThrows;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.Mockito.never;
// import static org.mockito.Mockito.verify;
// import static org.mockito.Mockito.when;

// import java.util.List;

// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.mindrot.jbcrypt.BCrypt;
// import org.mockito.ArgumentCaptor;
// import org.mockito.Mock;
// import org.mockito.MockedStatic;
// import org.mockito.Mockito;
// import org.mockito.MockitoAnnotations;

// import com.blogging_platform.classes.UserRecord;
// import com.blogging_platform.dao.interfaces.UserDAO;
// import com.blogging_platform.exceptions.AuthenticationException;
// import com.blogging_platform.exceptions.DuplicateEmailException;
// import com.blogging_platform.model.User;

// /**
//  * Unit tests for {@link UserService}.
//  *
//  * Focuses on registration and login behaviour, including password hashing
//  * and DAO interaction.
//  */
// class UserServiceTest {

//     @Mock
//     private UserDAO userDAO;

//     private UserService userService;

//     @BeforeEach
//     void setUp() {
//         MockitoAnnotations.openMocks(this);
//         userService = new UserService(userDAO);
//     }

//     @Test
//     void registerUser_hashesPasswordAndSaves_whenEmailNotExisting() throws DuplicateEmailException {
//         User user = new User("John Doe", "john@example.com", "plainPassword", "USER");

//         when(userDAO.existsByEmail(user.getEmail())).thenReturn(false);

//         try (MockedStatic<BCrypt> bCryptMock = Mockito.mockStatic(BCrypt.class)) {
//             bCryptMock.when(() -> BCrypt.gensalt()).thenReturn("salt");
//             bCryptMock.when(() -> BCrypt.hashpw("plainPassword", "salt")).thenReturn("hashedPassword");

//             userService.registerUser(user);
//         }

//         ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
//         verify(userDAO).register(captor.capture());

//         User saved = captor.getValue();
//         // Original user object should now contain the hashed password
//         assertEquals("hashedPassword", saved.getPassword());
//     }

//     @Test
//     void registerUser_throwsDuplicateEmail_whenEmailAlreadyExists() {
//         User user = new User("John Doe", "john@example.com", "plainPassword", "USER");
//         when(userDAO.existsByEmail(user.getEmail())).thenReturn(true);

//         assertThrows(DuplicateEmailException.class, () -> userService.registerUser(user));

//         verify(userDAO, never()).register(any(User.class));
//     }

//     @Test
//     void loginUser_returnsUserRecord_whenCredentialsValid() throws AuthenticationException {
//         String email = "john@example.com";
//         String password = "plainPassword";
//         UserRecord record = new UserRecord("id-1", "John Doe", email, "USER");
//         when(userDAO.login(email, password)).thenReturn(record);

//         UserRecord result = userService.loginUser(email, password);

//         assertEquals(record, result);
//         verify(userDAO).login(email, password);
//     }

//     @Test
//     void loginUser_throwsAuthenticationException_whenCredentialsInvalid() {
//         String email = "john@example.com";
//         String password = "badPassword";
//         when(userDAO.login(email, password)).thenReturn(null);

//         assertThrows(AuthenticationException.class, () -> userService.loginUser(email, password));
//     }

//     @Test
//     void existsById_delegatesToDao() {
//         String userId = "user-1";
//         when(userDAO.existsById(userId)).thenReturn(true);

//         boolean result = userService.existsById(userId);

//         assertEquals(true, result);
//         verify(userDAO).existsById(userId);
//     }

//     @Test
//     void getUsers_delegatesToDao() {
//         List<UserRecord> records = List.of(new UserRecord("id-1", "John", "john@example.com", "USER"));
//         when(userDAO.findAll()).thenReturn(records);

//         List<UserRecord> result = userService.getUsers();

//         assertEquals(records, result);
//         verify(userDAO).findAll();
//     }

//     @Test
//     void logoutUser_delegatesToDao() {
//         userService.logoutUser();

//         verify(userDAO).logout();
//     }
// }

