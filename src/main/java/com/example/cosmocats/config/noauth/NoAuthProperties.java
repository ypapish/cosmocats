package com.example.cosmocats.config.noauth;

import java.util.Map;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Profile;

@Data
@NoArgsConstructor
@Profile("no-auth")
@ConfigurationProperties(prefix = "auth")
public class NoAuthProperties {
  private Map<String, Object> claims;
}
