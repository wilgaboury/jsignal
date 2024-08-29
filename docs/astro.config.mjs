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
              label: "Why JSignal?",
              slug: "start/why",
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
              label: "Hotswap Setup",
              slug: "guides/hotswap",
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
