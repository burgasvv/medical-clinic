
import {IdentityDependency, IdentityRequest} from './identity.model';
import {AppointmentDependencyInPatient} from './appointment.model';

export interface PatientRequest {
    id: string | null;
    identity: IdentityRequest | null;
    passport: string | null;
}

export interface PatientDependency {
    id: string | null;
    identity: IdentityDependency | null;
    passport: string | null;
    createdAt: Date | null;
}

export interface PatientDependencyInIdentity {
    id: string | null;
    passport: string | null;
    createdAt: Date | null;
}

export interface PatientResponse {
    id: string | null;
    identity: IdentityDependency | null;
    passport: string | null;
    createdAt: Date | null;
    appointments: AppointmentDependencyInPatient[] | null;
}
