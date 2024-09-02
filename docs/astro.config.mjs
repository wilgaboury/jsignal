import { defineConfig } from "astro/config";
import starlight from "@astrojs/starlight";

import tailwind from "@astrojs/tailwind";

// https://astro.build/config
export default defineConfig({
  integrations: [
    starlight({
      title: "JSignal",
      logo: {
        src: "./src/assets/logo.svg",
      },
      social: {
        github: "https://github.com/wilgaboury/jsignal",
        discord: "https://discord.gg/YN7tek3CM2",
      },
      sidebar: [
        {
          label: "Getting Started",
          items: [
            {
              label: "Install",
              slug: "start/install",
            },
            {
              label: "Why JSignal?",
              slug: "start/why",
            },
            {
              label: "Hotswap Setup",
              slug: "start/hotswap",
            },
          ],
        },
        {
          label: "Guides",
          items: [
            {
              label: "Signals and Effects",
              slug: "guides/reactive",
            },
            {
              label: "Hotswap Details",
              slug: "guides/hotswap",
            },
            {
              label: "Context API",
              slug: "guides/context",
            },
          ],
        },
      ],
      customCss: ["./src/tailwind.css"],
    }),
    tailwind({
      applyBaseStyles: false,
    }),
  ],
});
