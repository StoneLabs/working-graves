<img alt="GitHub Workflow Status (branch)" src="https://img.shields.io/github/actions/workflow/status/StoneLabs/working-graves/build.yml?branch=master&label=master&style=flat-square"> <img alt="GitHub issues" src="https://img.shields.io/github/issues/StoneLabs/working-graves?style=flat-square"> <img alt="Version" src="https://img.shields.io/badge/Minecraft%20Version-1.20.2-blue?style=flat-square"> <img alt="GitHub Repo stars" src="https://img.shields.io/github/stars/StoneLabs/working-graves?style=flat-square"> <img alt="GitHub all releases" src="https://img.shields.io/github/downloads/StoneLabs/working-graves/total?color=gold&label=GH Downloads&style=flat-square"> <a href="https://modrinth.com/mod/working-graves"><img alt="Modrinth all releases" src="https://img.shields.io/modrinth/dt/GU61bZwi?color=gold&label=Modrinth Downloads&style=flat-square"></a>

<img src="https://user-images.githubusercontent.com/19885942/133668909-406ae7e7-3012-4444-b2c4-392c94d51837.png" align="right" width="250" />

# Working Graves

Yet another graves mod, I know. Working graves is a fabric mod that allows for the construction of graves that will be populated on player death. That means you can build a graveyard that will fill up as time goes on. Pretty cool huh?

Working graves is a server side mod. There is no need to install it on clients if you install it on your dedicated server. This means that, if installed on the server, it is completely vanilla compatible. Alternatively you can install in on the client for single player support.

## How to use

Follow these steps for success.
1. Place a sign with the text `hic portus animae`.
2. Right click the sign with a soul torch to underline it.
3. Place a barrel/chest under the sign (a 3x3x3 cube under the sign is checked) [optional]
4. ???
5. Die.

You can now go to the grave to collect your items.

Graves initially attempt to be used in the dimension where the death occurred. If no available grave is found in the current dimension, the system will iteratively search through other server dimensions in a random manner until an available grave is located. This behaviour can be toggled using gamerules.

## Server operators

You can see various debug information using the `/graves` command.
You can also use the `gravesRequireSoulTorch` gamerule to specify whether the soul torch is required for create a grave. The `gravesDoLightningFire` gamerule can be used to toggle whether the lightning can spawn fire and `graveInAllDimensions` toggles wether players can be graved in other dimensions. Additionally, a permission level at least equal to `gravesRequiredPermissionLevel` is required to create graves (defaults to 0).

## Download

[See the release page.](https://github.com/StoneLabs/working-graves/releases)

## License

This is free and unencumbered software released into the public domain.

Anyone is free to copy, modify, publish, use, compile, sell, or
distribute this software, either in source code form or as a compiled
binary, for any purpose, commercial or non-commercial, and by any
means.

In jurisdictions that recognize copyright laws, the author or authors
of this software dedicate any and all copyright interest in the
software to the public domain. We make this dedication for the benefit
of the public at large and to the detriment of our heirs and
successors. We intend this dedication to be an overt act of
relinquishment in perpetuity of all present and future rights to this
software under copyright law.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
OTHER DEALINGS IN THE SOFTWARE.

For more information, please refer to <http://unlicense.org/>
