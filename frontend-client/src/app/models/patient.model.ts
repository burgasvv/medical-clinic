
import {IdentityRequest, IdentityResponse} from './identity.model';
import {AppointmentDependencyInPatient} from './appointment.model';

export interface PatientRequest {
    id: string | null;
    identity: IdentityRequest | null;
    passport: string | null;
}

export interface PatientDependency {
    id: string | null;
    identity: IdentityResponse | null;
    passport: string | null;
    createdAt: Date | null;
}

export interface PatientResponse {
    id: string | null;
    identity: IdentityResponse | null;
    passport: string | null;
    createdAt: Date | null;
    appointments: AppointmentDependencyInPatient[] | null;
}
