package dev.kurama.api.core.hateoas.processor;

import static dev.kurama.api.core.authority.AuthorityAuthority.AUTHORITY_READ;
import static dev.kurama.api.core.hateoas.relations.AuthorityRelations.AUTHORITIES_REL;
import static dev.kurama.api.core.utility.AuthorityUtils.hasAuthority;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import dev.kurama.api.core.hateoas.model.AuthorityModel;
import dev.kurama.api.core.rest.AuthorityController;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;

@Component
public class AuthorityModelProcessor extends DomainModelProcessor<AuthorityModel> {

  @Override
  public @NonNull AuthorityModel process(@NonNull AuthorityModel entity) {
    return !hasAuthority(AUTHORITY_READ) ? entity : entity
      .add(getModelSelfLink(entity.getId()))
      .add(getParentLink());
  }

  @Override
  protected Class<AuthorityController> getClazz() {
    return AuthorityController.class;
  }

  @SneakyThrows
  @Override
  public WebMvcLinkBuilder getSelfLink(String id) {
    return linkTo(methodOn(getClazz()).get(id));
  }

  private @NonNull Link getParentLink() {
    return linkTo(methodOn(getClazz()).getAll(null)).withRel(AUTHORITIES_REL);
  }
}