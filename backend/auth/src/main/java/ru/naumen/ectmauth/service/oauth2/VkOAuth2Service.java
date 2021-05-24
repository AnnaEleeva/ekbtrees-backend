package ru.naumen.ectmauth.service.oauth2;

import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.UserAuthResponse;
import com.vk.api.sdk.objects.users.responses.GetResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.naumen.ectmauth.config.OAuth2Config;
import ru.naumen.ectmauth.constants.AuthConstants;
import ru.naumen.ectmauth.entity.Provider;
import ru.naumen.ectmauth.entity.User;
import ru.naumen.ectmauth.repository.RoleRepository;
import ru.naumen.ectmauth.repository.UserRepository;
import ru.naumen.ectmauth.service.TokensService;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

import static ru.naumen.ectmauth.utils.CookieUtils.setTokenCookies;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class VkOAuth2Service implements OAuth2Service {

    private final UserRepository userRepository;
    private final TokensService tokensService;
    private final OAuth2Config oAuth2Config;
    private final VkApiClient vk;
    private final RoleRepository roleRepository;

    @Override
    public String getOAuthUri() {
        return "https://oauth.vk.com/authorize?client_id=" +
                oAuth2Config.getVkAppId() +
                "&display=page&redirect_uri=" +
                oAuth2Config.getBaseUrl() +
                "/auth/oauth2/vk/callback" +
                "&scope=groups&response_type=code";
    }

    @Override
    public void handleOAuthFlow(String authorizationCode, HttpServletResponse response) {

        UserAuthResponse authResponse;
        try {
            authResponse = vk.oAuth().userAuthorizationCodeFlow(Integer.parseInt(oAuth2Config.getVkAppId()), oAuth2Config.getVkSecretKey(), getRedirectUri(), authorizationCode).execute();
        } catch (ApiException | ClientException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        User user = userRepository.findByVkId(Long.valueOf(authResponse.getUserId()));
        if (user == null) {
            user = createNewUser(authResponse.getUserId(), authResponse.getAccessToken());
        }

        setTokenCookies(response, tokensService.getTokensByOAuth2(user));
    }

    private String getRedirectUri() {
        return oAuth2Config.getBaseUrl() + "/auth/oauth2/vk/callback";
    }

    private User createNewUser(Integer userVkId, String token) {
        try {
            UserActor actor = new UserActor(userVkId, token);
            List<GetResponse> getUsersResponse = vk.users().get(actor).userIds(String.valueOf(userVkId)).execute();
            GetResponse userR = getUsersResponse.get(0);

            User user = new User();
            user.setFirstName(userR.getFirstName());
            user.setLastName(userR.getLastName());
            user.setRoles(roleRepository.findAllByNames(AuthConstants.DEFAULT_ROLE_NAMES));
            user.setVkId(Long.valueOf(userR.getId()));
            user.setProvider(Provider.vk);

            userRepository.save(user);

            return user;

        } catch (ApiException | ClientException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
