
import {IdentityDependency, IdentityRequest} from './identity.model';

export interface AdminRequest {
    id: string | null;
    identity: IdentityRequest | null;
}

export interface AdminDependencyInIdentity {
    id: string | null;
    createdAt: Date | null;
}

export interface AdminResponse {
    id: string | null;
    identity: IdentityDependency | null;
    createdAt: Date | null;
}
