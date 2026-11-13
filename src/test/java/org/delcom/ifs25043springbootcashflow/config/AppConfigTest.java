package org.delcom.ifs25043springbootcashflow.config;

import org.delcom.ifs25043springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25043springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25043springbootcashflow.domain.repository.ITransactionRepository;
import org.delcom.ifs25043springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25043springbootcashflow.usecase.AuthUseCase;
import org.delcom.ifs25043springbootcashflow.usecase.FinanceUseCase;
import org.delcom.ifs25043springbootcashflow.usecase.UserUseCase;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;

import javax.sql.DataSource;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AppConfigTest {

    @TempDir
    Path tempDir;

    @Test
    void wiresBeans() throws Exception {
        AppConfig config = new AppConfig();
        DataSource dataSource = createDataSource();

        IUserRepository userRepository = config.userRepository(dataSource);
        ITransactionRepository transactionRepository = config.transactionRepository(dataSource);
        FinanceUseCase financeUseCase = config.financeUseCase(transactionRepository);
        AuthUseCase authUseCase = config.authUseCase(userRepository);
        UserUseCase userUseCase = config.userUseCase(userRepository);
        FinancePresenter presenter = config.financePresenter();
        FileStorage fileStorage = config.fileStorage(tempDir.toString());

        assertNotNull(userRepository);
        assertNotNull(transactionRepository);
        assertNotNull(financeUseCase);
        assertNotNull(authUseCase);
        assertNotNull(userUseCase);
        assertNotNull(presenter);
        assertNotNull(fileStorage);
        assertInstanceOf(ITransactionRepository.class, transactionRepository);

        config.seedRootAdmin(authUseCase, "admin", "admin")
                .run(new DefaultApplicationArguments());

        ResourceHandlerRegistry registry = mock(ResourceHandlerRegistry.class);
        ResourceHandlerRegistration registration = mock(ResourceHandlerRegistration.class);
        when(registry.addResourceHandler(anyString())).thenReturn(registration);
        when(registration.addResourceLocations(anyString())).thenReturn(registration);
        config.uploadResourceConfigurer(fileStorage).addResourceHandlers(registry);
        verify(registry).addResourceHandler("/uploads/**");
        verify(registration).addResourceLocations(anyString());
    }

    @Test
    void uploadResourceHandlerAddsSlashForMissingDirectory() {
        AppConfig config = new AppConfig();
        FileStorage missingDirectory = config.fileStorage(tempDir.resolve("belum-ada").toString());

        ResourceHandlerRegistry registry = mock(ResourceHandlerRegistry.class);
        ResourceHandlerRegistration registration = mock(ResourceHandlerRegistration.class);
        when(registry.addResourceHandler(anyString())).thenReturn(registration);
        when(registration.addResourceLocations(anyString())).thenReturn(registration);

        config.uploadResourceConfigurer(missingDirectory).addResourceHandlers(registry);

        verify(registry).addResourceHandler("/uploads/**");
        verify(registration).addResourceLocations(anyString());
    }

    private DataSource createDataSource() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:appconfig_" + System.nanoTime()
                + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE");
        dataSource.setUser("sa");
        dataSource.setPassword("");
        return dataSource;
    }
}
