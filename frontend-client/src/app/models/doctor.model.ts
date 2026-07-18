
import {IdentityRequest, IdentityResponse} from './identity.model';
import {ImageResponse} from './image.model';
import {CategoryDependency} from './category.model';
import {ServiceDependency} from './service.model';
import {ScheduleDependencyInDoctor} from './schedule.model';

export interface DoctorRequest {
    id: string | null;
    identity: IdentityRequest | null;
    categoryId: string | null;
    about: string | null;
}

export interface DoctorDependency {
    id: string | null;
    identity: IdentityResponse | null;
    category: string | null;
    about: string | null;
    image: ImageResponse | null;
    createdAt: Date | null;
}

export interface DoctorResponse {
    id: string | null;
    identity: IdentityResponse | null;
    category: CategoryDependency | null;
    about: string | null;
    image: ImageResponse | null;
    createdAt: Date | null;
    services: ServiceDependency[] | null;
    schedules: ScheduleDependencyInDoctor[] | null;
}

export interface DoctorServiceRequest {
    doctorId: string;
    serviceId: string;
}
