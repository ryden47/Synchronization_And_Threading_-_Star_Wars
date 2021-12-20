package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;

import java.util.ArrayList;
import java.util.List;

public class AttackEvent implements Event<Boolean> {
    final List<Integer> serials;
    final int duration;

    public AttackEvent(List<Integer> _serials, int _duration) {
        serials = _serials;
        duration = _duration;
    }

    public AttackEvent(){
        serials = new ArrayList<>();
        duration = 0;
    }

    public int getDuration() {
        return duration;
    }

    public List<Integer> getSerials() {
        return serials;
    }
}
