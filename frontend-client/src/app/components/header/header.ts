import {Component, inject, OnInit} from '@angular/core';
import {IdentityService} from '../../services/identity-service/identity-service';

@Component({
    selector: 'app-header',
    standalone: false,
    templateUrl: './header.html',
    styleUrl: './header.scss',
})
export class Header implements OnInit {

    public identityService = inject(IdentityService)

    ngOnInit(): void {
        this.identityService.findAuthenticated()
    }
}
