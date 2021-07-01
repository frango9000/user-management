package dev.kurama.chess.backend.auth.api.domain.model;

import dev.kurama.chess.backend.core.api.domain.DomainModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class UserModel extends RepresentationModel<UserModel> implements DomainModel {

  private String id;
}
