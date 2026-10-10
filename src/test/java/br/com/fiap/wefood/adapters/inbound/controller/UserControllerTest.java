package br.com.fiap.wefood.adapters.inbound.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;

import br.com.fiap.wefood.application.usecases.UserUseCase;

class UserControllerTest {

    /**
     * Spring Data's {@code PageRequest}/{@code Pageable} is zero-indexed —
     * page 0 is the first page. The controller used to default a missing
     * {@code page} query parameter to 1, silently skipping the first page
     * of results (e.g. "GET /users" with no params and &lt;= size users
     * would return an empty list even though users exist). It must default
     * to 0, matching both Spring Data's convention and the documented API
     * contract ("Page index, default 0").
     */
    @Test
    void getUsers_withNoPageParam_defaultsToPageZero() {
        UserUseCase userUseCase = mock(UserUseCase.class);
        when(userUseCase.getUsers(anyInt(), anyInt())).thenReturn(List.of());
        UserController controller = new UserController(userUseCase);

        controller.getUsers(null, null);

        verify(userUseCase).getUsers(0, 10);
    }

    @Test
    void getUsers_withExplicitPageParam_isPassedThrough() {
        UserUseCase userUseCase = mock(UserUseCase.class);
        when(userUseCase.getUsers(anyInt(), anyInt())).thenReturn(List.of());
        UserController controller = new UserController(userUseCase);

        controller.getUsers(2, 25);

        verify(userUseCase).getUsers(2, 25);
    }
}
