package com.mycompany.controller;

import com.mycompany.model.entity.User;
import com.mycompany.model.entity.enums.Role;
import com.mycompany.service.IUserService;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserControllerTest {

    @Mock
    private IUserService userService;

    @Mock
    private FacesContext facesContext;

    @Mock
    private ExternalContext externalContext;

    private UserController userController;

    private User testUser;
    
    private MockedStatic<FacesContext> facesContextMockedStatic;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        userController = new UserController();

        // Manually inject the mock service using reflection
        java.lang.reflect.Field field = userController.getClass().getDeclaredField("userService");
        field.setAccessible(true);
        field.set(userController, userService);

        testUser = new User();
        testUser.setId("1");
        testUser.setUserName("testuser");
        testUser.setPassword("password");
        testUser.setRole(Role.CASHIER); // Default role for tests

        // Start static mocking of FacesContext
        facesContextMockedStatic = Mockito.mockStatic(FacesContext.class);
        facesContextMockedStatic.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

        // Mock the behavior of the FacesContext instance
        when(facesContext.getExternalContext()).thenReturn(externalContext);
        Map<String, Object> sessionMap = new HashMap<>();
        when(externalContext.getSessionMap()).thenReturn(sessionMap);
    }

    @AfterEach
    void tearDown() {
        // Stop static mocking
        facesContextMockedStatic.close();
    }

    @Test
    void testLoginSuccess() {
        // Arrange
        userController.setUsername("testuser");
        userController.setPassword("password");
        when(userService.authenticate("testuser", "password")).thenReturn(testUser);

        // Act
        String outcome = userController.login();

        // Assert
        assertEquals("home?faces-redirect=true", outcome);
        assertNotNull(externalContext.getSessionMap().get("user"));
        assertEquals(testUser, externalContext.getSessionMap().get("user"));
    }

    @Test
    void testLoginFailure() {
        // Arrange
        userController.setUsername("wronguser");
        userController.setPassword("wrongpassword");
        when(userService.authenticate("wronguser", "wrongpassword")).thenReturn(null);

        // Act
        String outcome = userController.login();

        // Assert
        assertNull(outcome);
        verify(facesContext).addMessage(eq(null), any(FacesMessage.class));
    }

    @Test
    void testLogout() {
        // Act
        String outcome = userController.logout();

        // Assert
        assertEquals("login?faces-redirect=true", outcome);
        verify(externalContext).invalidateSession();
    }
    
    @Test
    void testCheckLoginWhenNotLoggedIn() throws Exception {
        // Arrange
        // In setUp, the session map is already new and empty

        // Act
        userController.checkLogin();

        // Assert
        verify(externalContext).redirect("login.xhtml");
    }

    @Test
    void testCheckLoginWhenLoggedIn() throws Exception {
        // Arrange
        externalContext.getSessionMap().put("user", testUser);

        // Act
        userController.checkLogin();

        // Assert
        verify(externalContext, never()).redirect(anyString());
    }
    
    @Test
    void testGetCurrentUserWhenLoggedIn() {
        // Arrange
        externalContext.getSessionMap().put("user", testUser);

        // Act
        User currentUser = userController.getCurrentUser();

        // Assert
        assertEquals(testUser, currentUser);
    }

    @Test
    void testGetCurrentUserWhenNotLoggedIn() {
        // Arrange - session map is empty by default

        // Act
        User currentUser = userController.getCurrentUser();

        // Assert
        assertNull(currentUser);
    }

    @Test
    void testIsAdminWithAdminUser() {
        // Arrange
        User adminUser = new User();
        adminUser.setRole(Role.ADMIN);
        externalContext.getSessionMap().put("user", adminUser);

        // Act
        boolean isAdmin = userController.isAdmin();

        // Assert
        assertTrue(isAdmin);
    }

    @Test
    void testIsAdminWithNonAdminUser() {
        // Arrange
        testUser.setRole(Role.CASHIER);
        externalContext.getSessionMap().put("user", testUser);

        // Act
        boolean isAdmin = userController.isAdmin();

        // Assert
        assertFalse(isAdmin);
    }

    @Test
    void testIsAdminWithNoUser() {
        // Arrange - session map is empty by default

        // Act
        boolean isAdmin = userController.isAdmin();

        // Assert
        assertFalse(isAdmin);
    }

    @Test
    void testCheckAdminAccessWhenNotLoggedIn() throws Exception {
        // Arrange - session map is empty by default

        // Act
        userController.checkAdminAccess();

        // Assert
        verify(externalContext).redirect("login.xhtml");
    }

    @Test
    void testCheckAdminAccessWhenLoggedInAsNonAdmin() throws Exception {
        // Arrange
        testUser.setRole(Role.CASHIER);
        externalContext.getSessionMap().put("user", testUser);

        // Act
        userController.checkAdminAccess();

        // Assert
        verify(externalContext).redirect("home.xhtml");
    }

    @Test
    void testCheckAdminAccessWhenLoggedInAsAdmin() throws Exception {
        // Arrange
        User adminUser = new User();
        adminUser.setRole(Role.ADMIN);
        externalContext.getSessionMap().put("user", adminUser);

        // Act
        userController.checkAdminAccess();

        // Assert
        verify(externalContext, never()).redirect(anyString());
    }

    @Test
    void testCheckAdminAccessWithStorekeeper() throws Exception {
        // Arrange
        User storekeeperUser = new User();
        storekeeperUser.setRole(Role.STOREKEEPER);
        externalContext.getSessionMap().put("user", storekeeperUser);

        // Act
        userController.checkAdminAccess();

        // Assert
        verify(externalContext).redirect("home.xhtml");
    }
}
