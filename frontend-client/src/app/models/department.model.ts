
import {CategoryDependency} from './category.model';

export interface DepartmentRequest {
    id: string | null;
    name: string | null;
    description: string | null;
}

export interface DepartmentDependency {
    id: string | null;
    name: string | null;
    description: string | null;
}

export interface DepartmentResponse {
    id: string | null;
    name: string | null;
    description: string | null;
    categories: CategoryDependency[] | null;
}
