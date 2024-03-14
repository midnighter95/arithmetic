{
  description = "vector";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs = { self, nixpkgs, flake-utils }@inputs:
    let
      overlay = import ./overlay.nix;
    in
    flake-utils.lib.eachDefaultSystem
      (system:
        let
          pkgs = import nixpkgs { inherit system; overlays = [ overlay ]; };
          deps = with pkgs; [
            cmake
            zlib
            z3

            mill
            python3
            go
            ammonite
            metals
            gnused
            coreutils
            gnumake
            gnugrep
            which
            parallel
            protobuf
            ninja
            verilator
            antlr4
            numactl
            dtc
            espresso
            circt

            yarn
            mdl
          ];
        in
        {
          legacyPackages = pkgs;
          devShell = pkgs.mkShell { 
            buildInputs = deps;
          };
        }
      )
    // { inherit inputs; };
}
