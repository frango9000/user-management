import { HttpHeaderKey } from '@app/domain';
import { ContentTypeEnum } from '@hal-form-client';
import { InteractionObject } from '@pact-foundation/pact';
import { HTTPMethod } from '@pact-foundation/pact/src/common/request';
import { iso8601DateTimeWithMillis, string } from '@pact-foundation/pact/src/dsl/matchers';
import { bearer } from '../../utils/pact.utils';
import { jwtToken } from '../../utils/token.util';

const serviceLogs = {
  logs: string(),
  timestamp: iso8601DateTimeWithMillis(),
  _links: {
    self: {
      href: 'http://localhost/api/administration/service-logs',
    },
  },
  _templates: {
    default: {
      method: 'HEAD',
      properties: [],
    },
  },
};

export namespace GetServiceLogsPact {
  export const successful: InteractionObject = {
    state: 'stateless',
    uponReceiving: 'get service logs',
    withRequest: {
      method: HTTPMethod.GET,
      path: '/api/administration/service-logs',
      headers: {
        Accept: ContentTypeEnum.APPLICATION_JSON_HAL_FORMS,
        Authorization: bearer(jwtToken({ authorities: ['service-logs:read'] })),
      },
    },
    willRespondWith: {
      status: 200,
      headers: {
        [HttpHeaderKey.CONTENT_TYPE]: ContentTypeEnum.APPLICATION_JSON_HAL_FORMS,
      },
      body: { ...serviceLogs },
    },
  };

  export const with_delete: InteractionObject = {
    state: 'stateless',
    uponReceiving: 'get service logs with delete',
    withRequest: {
      method: HTTPMethod.GET,
      path: '/api/administration/service-logs',
      headers: {
        Accept: ContentTypeEnum.APPLICATION_JSON_HAL_FORMS,
        Authorization: bearer(jwtToken({ authorities: ['service-logs:read', 'service-logs:delete'] })),
      },
    },
    willRespondWith: {
      status: 200,
      headers: {
        [HttpHeaderKey.CONTENT_TYPE]: ContentTypeEnum.APPLICATION_JSON_HAL_FORMS,
      },
      body: {
        ...serviceLogs,
        _templates: {
          default: { method: 'HEAD', properties: [] },
          deleteServiceLogs: {
            method: 'DELETE',
            properties: [],
          },
        },
      },
    },
  };

  export const unauthorized: InteractionObject = {
    state: 'stateless',
    uponReceiving: 'get service logs unauthorized',
    withRequest: {
      method: HTTPMethod.GET,
      path: '/api/administration/service-logs',
      headers: {
        Accept: ContentTypeEnum.APPLICATION_JSON_HAL_FORMS,
        Authorization: bearer(jwtToken()),
      },
    },
    willRespondWith: {
      status: 401,
      body: {
        reason: 'Unauthorized',
        title: 'Insufficient permissions',
      },
    },
  };
}
