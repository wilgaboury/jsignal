---
title: Why JSignal?
description: Justification of this libraries existance
---

In short, because Java does not have a popular declarative GUI library, and building desktop applications should be fun and easy!

The declarative/reactive paradigm has become the defacto standard for modern GUI library design (React, Flutter, SwiftUI, Jetpack Compose, etc.). Java is one of the most popular languages, used extensively in enterprise and education, yet it's ecosystem of GUI solutions, like Swing, JavaFX and SWT, have not kept up with the times. This project aims to fill the gap by providing a modern set of features.

## Why Signals?

The fine-grained reactive system used by this library was directly inspired by [SolidJS](https://www.solidjs.com/), and is fundamentally different from frameworks like React and Flutter where state is tied to the components. Signals, the most basic state wrapper objects, are very flexible in their usage. They can be local, global, passed around, and stored in arbitrary data structures which makes them intuitive to work with and removes the need for third-party state management utilities which are pervasive in many other libraries.
