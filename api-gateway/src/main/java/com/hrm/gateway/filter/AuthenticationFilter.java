package com.hrm.gateway.filter;

import org.apache.http.HttpHeaders;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AbstractGatewayFilterFactory.NameConfig> {
    private final WebClient.Builder webClientBuilder;

    // Constructor để tiêm WebClient
    public AuthenticationFilter(WebClient.Builder webClientBuilder) {
        super(NameConfig.class);
        this.webClientBuilder = webClientBuilder;
    }

    public static class Config {
        // Cần thiết nhưng có thể để trống nếu không có cấu hình cụ thể
    }

    @Override
    public GatewayFilter apply(NameConfig config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            // 1. Kiểm tra xem request có header Authorization không
            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                // Trả về 401 nếu thiếu header
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing Authorization header");
            }

            // 2. Lấy Token
            String authHeader = request.getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
            if (!authHeader.startsWith("Bearer ")) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Authorization format");
            }
            final String token = authHeader.substring(7);

            // 3. Gọi Auth Service để xác thực Token
            // Sử dụng WebClient (Reactive) để gọi AUTH-SERVICE
            // Tên dịch vụ: lb://AUTH-SERVICE
            return webClientBuilder.build()
                    .get()
                    .uri("lb://AUTH-SERVICE/auth/validate?token=" + token)
                    .retrieve()
                    // Xử lý lỗi từ Auth Service (ví dụ: 401)
                    .onStatus(s -> s.value() == 401, clientResponse ->
                            Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Token"))
                    )
                    .bodyToMono(Boolean.class) // Nhận kết quả xác thực (true/false)
                    .flatMap(isValid -> {
                        if (isValid) {
                            // 4. Token hợp lệ: Cho phép request đi tiếp và thêm Header User Info
                            // 💡 Giả định Auth Service trả về thông tin user (ví dụ: UserID, Role)

                            // NOTE: Để đơn giản, ở đây ta chỉ giả định là token hợp lệ và cho đi tiếp.
                            // Trong thực tế, bạn sẽ gọi một API khác trả về User Details, sau đó thêm vào header.

                            // Example: Thêm User ID và Role vào request header
                            ServerHttpRequest modifiedRequest = request.mutate()
                                    .header("X-User-ID", "123") // Giả định ID được trích xuất
                                    .header("X-User-Role", "EMPLOYEE") // Giả định Role được trích xuất
                                    .build();

                            return chain.filter(exchange.mutate().request(modifiedRequest).build());
                        } else {
                            // Token không hợp lệ
                            return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token validation failed"));
                        }
                    });
        };
    }
}
