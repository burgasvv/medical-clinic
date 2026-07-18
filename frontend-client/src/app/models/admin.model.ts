
import {IdentityRequest, IdentityResponse} from './identity.model';

export interface AdminRequest {
    id: string | null;
    identity: IdentityRequest | null;
}

export interface AdminResponse {
    id: string | null;
    identity: IdentityResponse | null;
    createdAt: Date | null;
}
