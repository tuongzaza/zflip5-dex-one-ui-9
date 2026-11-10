package dev.opencover.mirror;
interface IDesktopMirror {
    void destroy() = 16777114;
    boolean selectSource(int source, int destination, IBinder projection) = 1;
}
