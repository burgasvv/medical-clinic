
import {DepartmentDependency} from './department.model';
import {DoctorDependency} from './doctor.model';

export interface CategoryRequest {
    id: string | null;
    name: string | null;
    description: string | null;
    departmentId: string | null;
}

export interface CategoryDependency {
    id: string | null;
    name: string | null;
    description: string | null;
}

export interface CategoryResponse {
    id: string | null;
    name: string | null;
    description: string | null;
    department: DepartmentDependency | null;
    doctors: DoctorDependency[] | null;
}
