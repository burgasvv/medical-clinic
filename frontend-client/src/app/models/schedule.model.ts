
import {DoctorDependency} from './doctor.model';
import {AppointmentDependencyInSchedule} from './appointment.model';

export interface ScheduleRequest {
    id: string | null;
    datetime: Date | null;
    doctorId: string | null;
    concluded: boolean | null;
}

export interface ScheduleDependencyInDoctor {
    id: string | null;
    datetime: Date | null;
    concluded: boolean | null;
    appointment: AppointmentDependencyInSchedule | null;
}

export interface ScheduleDependencyInAppointment {
    id: string | null;
    datetime: Date | null;
    concluded: boolean | null;
    doctor: DoctorDependency | null;
}

export interface ScheduleResponse {
    id: string | null;
    datetime: Date | null;
    concluded: boolean | null;
    doctor: DoctorDependency | null;
    appointment: AppointmentDependencyInSchedule | null;
}
