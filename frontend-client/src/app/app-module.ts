import { NgModule, provideBrowserGlobalErrorListeners } from '@angular/core';
import { BrowserModule, provideClientHydration } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing-module';
import { App } from './app';
import { CommonModule } from '@angular/common';
import { Header } from './components/header/header';
import { Footer } from './components/footer/footer';
import { Main } from './components/main/main';
import { Service } from './components/service/service';
import { ServiceItem } from './components/service-item/service-item';
import {provideHttpClient, withInterceptors} from '@angular/common/http';
import { Auth } from './components/auth/auth';
import {authInterceptor} from './services/auth-service/auth-interceptor';
import {FormsModule} from "@angular/forms";

@NgModule({
    declarations: [App, Header, Footer, Main, Service, ServiceItem, Auth],
    imports: [BrowserModule, AppRoutingModule, CommonModule, FormsModule],
    providers: [
        provideBrowserGlobalErrorListeners(),
        provideClientHydration(),
        provideHttpClient(withInterceptors([authInterceptor])),
    ],
    bootstrap: [App],
})
export class AppModule {}
