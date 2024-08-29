---
title: Why JSignal?
description: A guide in my new Starlight docs site.
---

The declarative/reactive paradigm has become the defacto standard for modern GUI library design (React, Flutter, SwiftUI, Jetpack Compose, etc.). Java is one of the most popular languages, used extensively in education and enterprise development, yet it lacks any popular libraries in this category. Traditional imperative libraries like Swing, JavaFX and SWT feel quite outdated and do not use modern native graphics backends (DirectX12, Metal, Vulkan). This project's goal is to build a modern, declarative Java library for graphical desktop application development.

This projects reactive system is highly inspired by [SolidJS](https://www.solidjs.com/). I have the upmost respect for frameworks like React and Flutter, both of which have been influential on this project in many ways. But I find that the component/widget based state and tree diffing (VDOM) used by these frameworks often introduces unneccesary complexity to what should be simple problems. A fine-grain reactive model on the other hand allows for state to treated like any other variables and automatically tracked when accessed, which leads to more intuitive code.
