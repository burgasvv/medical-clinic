
import {Authority} from './authority.model';
import {AdminDependencyInIdentity} from './admin.model';
import {PatientDependencyInIdentity} from './patient.model';
import {DoctorDependencyInIdentity} from './doctor.model';

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

export interface IdentityDependency {
    id: string | null;
    authority: Authority | null;
    email: string | null;
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
    admin: AdminDependencyInIdentity | null;
    patient: PatientDependencyInIdentity | null;
    doctor: DoctorDependencyInIdentity | null;
}
