import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {CommonModule} from '@angular/common';
import {Main} from './components/main/main';
import {Service} from './components/service/service';
import {ServiceItem} from './components/service-item/service-item';

const routes: Routes = [
    {path: '', component: Main},
    {path: 'services', component: Service},
    {path: 'services/:id', component: ServiceItem}
];

@NgModule({
    imports: [RouterModule.forRoot(routes), CommonModule],
    exports: [RouterModule]
})
export class AppRoutingModule {
}
