
import {Authority} from './authority.model';

export interface IdentityRequest {
    id: string | null;
    authority: Authority | null;
    email: string | null;
    password: string | null;
    phone: string | null;
    status: boolean | null;
    firstname: string | null;
    lastname: string | null;
    patronymic: string | null;
}

export interface IdentityResponse {
    id: string | null;
    authority: Authority | null;
    email: string | null;
    phone: string | null;
    status: boolean | null;
    firstname: string | null;
    lastname: string | null;
    patronymic: string | null;
}
