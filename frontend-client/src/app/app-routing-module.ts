import {inject, NgModule} from '@angular/core';
import {Router, RouterModule, Routes} from '@angular/router';
import {CommonModule} from '@angular/common';
import {Main} from './components/main/main';
import {Service} from './components/service/service';
import {ServiceItem} from './components/service-item/service-item';
import {Auth} from './components/auth/auth';
import {AuthService} from './services/auth-service/auth-service';

const routes: Routes = [
    {path: '', component: Main},
    {path: 'login', component: Auth},
    {
        path: 'logout',
        canActivate: [
            () => {
                const authService = inject(AuthService)
                const router = inject(Router)
                authService.logout().subscribe({
                    next: value => {
                        console.log(value)
                        router.navigateByUrl('').then(r => r)
                    },
                    error: err => console.log(err)
                })
            }
        ],
        children: []
    },
    {path: 'services', component: Service},
    {path: 'services/:id', component: ServiceItem}
];

@NgModule({
    imports: [RouterModule.forRoot(routes), CommonModule],
    exports: [RouterModule]
})
export class AppRoutingModule {
}
