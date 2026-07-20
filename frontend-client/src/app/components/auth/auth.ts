import {Component, inject} from '@angular/core';
import {AuthService} from '../../services/auth-service/auth-service';
import {Router} from '@angular/router';
import {NgForm} from '@angular/forms';

@Component({
    selector: 'app-auth',
    standalone: false,
    templateUrl: './auth.html',
    styleUrl: './auth.scss',
})
export class Auth {

    private authService = inject(AuthService)
    private router = inject(Router)

    onLogin(form: NgForm) {
        this.authService.login(form).subscribe({
            next: value => {
                console.log(value)
                this.router.navigateByUrl('').then(r => r)
            },
            error: err => console.log(err)
        })
    }

    onLogout() {
        this.authService.logout().subscribe({
            next: value => {
                console.log(value)
                this.router.navigateByUrl('').then(r => r)
            },
            error: err => console.log(err)
        })
    }
}
