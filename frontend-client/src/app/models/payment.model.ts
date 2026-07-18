
import {AppointmentDependencyInPayment} from './appointment.model';

export interface PaymentRequest {
    id: string | null;
    appointmentId: string
}

export interface PaymentDependency {
    id: string | null;
    price: number | null;
    createdAt: Date | null;
}

export interface PaymentResponse {
    id: string | null;
    appointment: AppointmentDependencyInPayment | null;
    price: number | null;
    createdAt: Date | null;
}
