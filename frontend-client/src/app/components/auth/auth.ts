import {Component, inject} from '@angular/core';
import {AuthService} from '../../services/auth-service/auth-service';

@Component({
    selector: 'app-auth',
    standalone: false,
    templateUrl: './auth.html',
    styleUrl: './auth.scss',
})
export class Auth {

    private authService = inject(AuthService)
}
