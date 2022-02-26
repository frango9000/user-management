import { randomUUID } from 'crypto';
import * as jwt from 'jsonwebtoken';

export interface AuthUser {
  user?: {
    id: string;
    username: string;
  };
  authorities?: string[];
}

export function generateToken(authUser: AuthUser = {}): string {
  const { user, authorities = [] } = authUser;
  const payload = {
    user: {
      id: user?.id || randomUUID(),
      username: user?.username || randomUUID().slice(0, 8),
    },
    authorities,
  };
  return jwt.sign(payload, 'pact-secret', {
    algorithm: 'HS512',
    issuer: 'api',
    audience: 'app',
    expiresIn: '2h',
    subject: payload.user.username,
  });
}
