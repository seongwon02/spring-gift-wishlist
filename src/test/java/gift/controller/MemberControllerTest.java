package gift.controller;

import gift.dto.MemberRequestDto;
import gift.dto.TokenResponseDto;
import gift.service.MemberService;
import gift.util.JwtTokenProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MemberControllerTest {

    @Autowired
    private MemberService memberService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @LocalServerPort
    private int port;

    private RestClient client;
    private String url;

    @BeforeAll
    void setUp() {
        client = RestClient.builder().build();
        url = "http://localhost:" + port + "/api/members";
    }

    @DisplayName("정상적인 회원가입")
    @Test
    void 정상적인_회원가입() {

        MemberRequestDto requestDto = new MemberRequestDto(
                "user3@example.com",
                "0000"
        );

        var response = client.post()
                .uri(url + "/register")
                .body(requestDto)
                .retrieve()
                .toEntity(TokenResponseDto.class);

        TokenResponseDto dtoBody = response.getBody();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        assert dtoBody != null;
        assertThat(jwtTokenProvider.getIdFromToken(dtoBody.getToken())).isEqualTo("4");
    }

    @DisplayName("정상적인 로그인")
    @Test
    void 정상적인_로그인() {

        MemberRequestDto requestDto = new MemberRequestDto(
                "admin@example.com",
                "1234"
        );

        var response = client.post()
                .uri(url + "/login")
                .body(requestDto)
                .retrieve()
                .toEntity(TokenResponseDto.class);

        TokenResponseDto dtoBody = response.getBody();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        assert dtoBody != null;
        assertThat(jwtTokenProvider.getIdFromToken(dtoBody.getToken())).isEqualTo("1");
    }

    @DisplayName("회원가입할 때 이메일 형식이 아닌 문자열을 보낼 시, 400 에러코드와 메세지 반환하는지 테스트")
    @Test
    void 잘못된_이메일을_입력하는_회원가입() {

        MemberRequestDto requestDto = new MemberRequestDto(
                "useremail.com",
                "0000"
        );
        assertThatExceptionOfType(HttpClientErrorException.BadRequest.class)
                .isThrownBy(
                        () ->
                                client.post()
                                        .uri(url + "/register")
                                        .body(requestDto)
                                        .retrieve()
                                        .toEntity(Void.class)
                )
                .satisfies(ex -> {
                    String body = ex.getResponseBodyAsString();
                    assertThat(body).contains("이메일 형식과 맞지 않습니다.");
                });
    }

    @DisplayName("회원가입할 때 이메일이 중복되면, 409에러코드와 메세지 반환하는지 테스트")
    @Test
    void 회원가입시_이메일이_중복() {

        MemberRequestDto requestDto = new MemberRequestDto(
                "admin@example.com",
                "0000"
        );
        assertThatExceptionOfType(HttpClientErrorException.Conflict.class)
                .isThrownBy(
                        () ->
                                client.post()
                                        .uri(url + "/register")
                                        .body(requestDto)
                                        .retrieve()
                                        .toEntity(Void.class)
                )
                .satisfies(ex -> {
                    String body = ex.getResponseBodyAsString();
                    assertThat(body).contains("이미 해당 이메일로 가입된 회원이 존재합니다.");
                });
    }

    @DisplayName("로그인할 때 이메일 형식이 아닌 문자열을 보낼 시, 400 에러코드와 메세지 반환하는지 테스트")
    @Test
    void 잘못된_이메일을_입력하는_로그인() {

        MemberRequestDto requestDto = new MemberRequestDto(
                "useremail.com",
                "0000"
        );
        assertThatExceptionOfType(HttpClientErrorException.BadRequest.class)
                .isThrownBy(
                        () ->
                                client.post()
                                        .uri(url + "/login")
                                        .body(requestDto)
                                        .retrieve()
                                        .toEntity(Void.class)
                )
                .satisfies(ex -> {
                    String body = ex.getResponseBodyAsString();
                    assertThat(body).contains("이메일 형식과 맞지 않습니다.");
                });
    }

    @DisplayName("로그인시 이메일이 DB에 존재하지 않는 경우, 401 에러코드와 메세지 반환하는지 테스트")
    @Test
    void 로그인시_이메일이_존재하지_않는_경우() {

        MemberRequestDto requestDto = new MemberRequestDto(
                "adminadmin@example.com",
                "0000"
        );
        assertThatExceptionOfType(HttpClientErrorException.Unauthorized.class)
                .isThrownBy(
                        () ->
                                client.post()
                                        .uri(url + "/login")
                                        .body(requestDto)
                                        .retrieve()
                                        .toEntity(Void.class)
                )
                .satisfies(ex -> {
                    String body = ex.getResponseBodyAsString();
                    assertThat(body).contains("아이디 또는 비밀번호가 잘못 되었습니다. 아이디와 비밀번호를 정확히 입력해 주세요.");
                });
    }

    @DisplayName("로그인시 비밀번호가 틀리면, 401 에러코드와 메세지 반환하는지 테스트")
    @Test
    void 로그인시_비밀번호가_틀린_경우() {

        MemberRequestDto requestDto = new MemberRequestDto(
                "admin@example.com",
                "0000"
        );
        assertThatExceptionOfType(HttpClientErrorException.Unauthorized.class)
                .isThrownBy(
                        () ->
                                client.post()
                                        .uri(url + "/login")
                                        .body(requestDto)
                                        .retrieve()
                                        .toEntity(Void.class)
                )
                .satisfies(ex -> {
                    String body = ex.getResponseBodyAsString();
                    assertThat(body).contains("아이디 또는 비밀번호가 잘못 되었습니다. 아이디와 비밀번호를 정확히 입력해 주세요.");
                });
    }

}
