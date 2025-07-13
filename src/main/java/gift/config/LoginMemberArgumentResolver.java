package gift.config;

import gift.annotation.CurrentMember;
import gift.repository.MemberRepository;
import gift.util.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Component
public class LoginMemberArgumentResolver implements HandlerMethodArgumentResolver {

    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public LoginMemberArgumentResolver(MemberRepository memberRepository, JwtTokenProvider jwtTokenProvider) {
        this.memberRepository = memberRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentMember.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) throws Exception {
        HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();

        Optional<String> token = jwtTokenProvider.resolveAccessToken(request);

        if (token.isEmpty() || !jwtTokenProvider.validateToken(token.get())) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "토큰이 존재하지 않거나 유효하지 않습니다."
                    );
        }

        long id = Long.parseLong(jwtTokenProvider.getIdFromToken(token.get()));

        return memberRepository.findMemberById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "해당 ID의 멤버은 존재하지 않습니다."
                        )
                );
    }
}
