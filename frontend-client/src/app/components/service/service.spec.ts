import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Service } from './service';

describe('Service', () => {
    let component: Service;
    let fixture: ComponentFixture<Service>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [Service],
        }).compileComponents();

        fixture = TestBed.createComponent(Service);
        component = fixture.componentInstance;
        await fixture.whenStable();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
