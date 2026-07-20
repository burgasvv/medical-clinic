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
import { provideHttpClient } from '@angular/common/http';
import { Auth } from './components/auth/auth';

@NgModule({
    declarations: [App, Header, Footer, Main, Service, ServiceItem, Auth],
    imports: [BrowserModule, AppRoutingModule, CommonModule],
    providers: [
        provideBrowserGlobalErrorListeners(),
        provideClientHydration(),
        provideHttpClient(),
    ],
    bootstrap: [App],
})
export class AppModule {}
