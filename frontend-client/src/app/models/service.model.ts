
import {DoctorDependency} from './doctor.model';

export interface ServiceRequest {
    id: string | null;
    name: string | null;
    description: string | null;
    price: number | null;
}

export interface ServiceDependency {
    id: string | null;
    name: string | null;
    description: string | null;
    price: number | null;
}

export interface ServiceResponse {
    id: string | null;
    name: string | null;
    description: string | null;
    price: number | null;
    doctors: DoctorDependency[] | null;
}
