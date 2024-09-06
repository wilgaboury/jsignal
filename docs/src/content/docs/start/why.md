---
title: Why JSignal?
description: Justification of this libraries existance
---

Because Java does not have a popular declarative GUI library, and building desktop applications should be fun and easy!

The declarative/reactive paradigm has become the defacto standard for modern GUI library design. Java is one of the most popular languages, used extensively in enterprise and education, yet its ecosystem of GUI solutions, like Swing, JavaFX and SWT, have not kept up with the times. This project's goal is to fill the gap by providing a modern set of features.

## Signals

The reactive system used by this library was directly inspired by [SolidJS](https://www.solidjs.com/), and at it's core, is based on signals and effects. Signals are wrappers that all application state lives within. Unlike systems where state is tied to components, signals are very flexible in their usage: they can be local, global, passed around, or stored in arbitrary data structures. This makes them intuitive to work with and removes the need for third-party state management utilities. Effects are procedures that track any signals that are accessed inside of them, and when those tracked signals change, the effect re-executes.

## Fine-grained Reactivity

Many popular declarative GUI frameworks (i.e. React, Flutter, SwiftUI, Jetpack Compose) work through some sort of diffing system. User made components are essentially a function that constructs and returns a lightweight tree of nodes, then a diffing algorithm is run against the new and previous tree which produces a number of surgical changes that are applied to an underlying retained node tree. In JSignal, there is no diffing; the underlying node tree is built incrementally by user made components with state being tied directly to the location in the tree take where changes take place. This reactive system goes all the way down to painting and layouting; for instance, simply accessing a signal inside of a node's paint method will cause the compositing procedure to subscribe to it.
