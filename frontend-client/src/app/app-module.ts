import { NgModule, provideBrowserGlobalErrorListeners } from '@angular/core';
import { BrowserModule, provideClientHydration } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing-module';
import { App } from './app';
import { CommonModule } from '@angular/common';
import { Header } from './components/header/header';
import { Footer } from './components/footer/footer';

@NgModule({
    declarations: [App, Header, Footer],
    imports: [BrowserModule, AppRoutingModule, CommonModule],
    providers: [provideBrowserGlobalErrorListeners(), provideClientHydration()],
    bootstrap: [App],
})
export class AppModule {}
