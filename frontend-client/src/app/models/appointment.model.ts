
import {PatientDependency} from './patient.model';
import {ServiceDependency} from './service.model';
import {DocumentResponse} from './document.model';
import {ScheduleDependencyInAppointment} from './schedule.model';
import {PaymentDependency} from './payment.model';

export interface AppointmentRequest {
    id: string | null;
    scheduleId: string;
    patientId: string;
    serviceId: string;
}

export interface AppointmentDependencyInSchedule {
    id: string | null;
    patient: PatientDependency | null;
    service: ServiceDependency | null;
    document: DocumentResponse | null;
    payment: PaymentDependency | null;
}

export interface AppointmentDependencyInPayment {
    id: string | null;
    schedule: ScheduleDependencyInAppointment | null;
    patient: PatientDependency | null;
    service: ServiceDependency | null;
    document: DocumentResponse | null;
}

export interface AppointmentDependencyInPatient {
    id: string | null;
    schedule: ScheduleDependencyInAppointment | null;
    service: ServiceDependency | null;
    document: DocumentResponse | null;
    payment: PaymentDependency | null;
}

export interface AppointmentResponse {
    id: string | null;
    schedule: ScheduleDependencyInAppointment | null;
    patient: PatientDependency | null;
    service: ServiceDependency | null;
    document: DocumentResponse | null;
    payment: PaymentDependency | null;
}
