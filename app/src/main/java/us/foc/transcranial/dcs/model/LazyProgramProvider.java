package us.foc.transcranial.dcs.model;

import android.content.Context;

import us.foc.transcranial.dcs.R;

public class LazyProgramProvider {

    private static ProgramEntity gamerProgram;
    private static ProgramEntity enduroProgram;
    private static ProgramEntity waveProgram;
    private static ProgramEntity pulseProgram;
    private static ProgramEntity noiseProgram;

    public static ProgramEntity getGamerProgram(Context context) {

        if (gamerProgram == null) {
            ProgramEntity.MetaData metaData = new ProgramEntity.MetaData(
                    ProgramEntity.PROGRAM_ID_GAMER,
                    context.getString(R.string.program_title_gamer),
                    context.getString(R.string.program_creator_gamer),
                    context.getString(R.string.program_desc_gamer));

            gamerProgram = new ProgramEntity.Builder(metaData, ProgramEntity.ProgramMode.DCS, 1500, 600, 20)
                    .withSham(false)
                    .withShamDuration(35)
                    .build();
        }
        return gamerProgram;
    }

    public static ProgramEntity getEnduroProgram(Context context) {

        if (enduroProgram == null) {
            ProgramEntity.MetaData metaData = new ProgramEntity.MetaData(
                    ProgramEntity.PROGRAM_ID_ENDURO,
                    context.getString(R.string.program_title_enduro),
                    context.getString(R.string.program_creator_enduro),
                    context.getString(R.string.program_desc_enduro));

            enduroProgram = new ProgramEntity.Builder(metaData, ProgramEntity.ProgramMode.DCS, 1700, 900, 20)
                    .withSham(false)
                    .withShamDuration(45)
                    .build();
        }
        return enduroProgram;
    }

    public static ProgramEntity getWaveProgram(Context context) {

        if (waveProgram == null) {
            ProgramEntity.MetaData metaData = new ProgramEntity.MetaData(
                    ProgramEntity.PROGRAM_ID_WAVE,
                    context.getString(R.string.program_title_wave),
                    context.getString(R.string.program_creator_wave),
                    context.getString(R.string.program_desc_wave));

            waveProgram = new ProgramEntity.Builder(metaData, ProgramEntity.ProgramMode.ACS, 1500, 1080, 30)
                    .withSham(false)
                    .withShamDuration(25)
                    .withBipolar(false)
                    .withCurrentOffset(100)
                    .withFrequency(1000L)
                    .build();
        }
        return waveProgram;
    }

    public static ProgramEntity getPulseProgram(Context context) {

        if (pulseProgram == null) {
            ProgramEntity.MetaData metaData = new ProgramEntity.MetaData(
                    ProgramEntity.PROGRAM_ID_PULSE,
                    context.getString(R.string.program_title_pulse),
                    context.getString(R.string.program_creator_pulse),
                    context.getString(R.string.program_desc_pulse));

            pulseProgram = new ProgramEntity.Builder(metaData, ProgramEntity.ProgramMode.PCS, 1500, 600, 15)
                    .withSham(false)
                    .withShamDuration(25)
                    .withBipolar(false)
                    .withCurrentOffset(1200)
                    .withFrequency(40000L)
                    .withDutyCycle(20L)
                    .build();
        }

        return pulseProgram;
    }

    public static ProgramEntity getNoiseProgram(Context context) {

        if (noiseProgram == null) {
            ProgramEntity.MetaData metaData = new ProgramEntity.MetaData(
                    ProgramEntity.PROGRAM_ID_NOISE,
                    context.getString(R.string.program_title_noise),
                    context.getString(R.string.program_creator_noise),
                    context.getString(R.string.program_desc_noise));

            noiseProgram = new ProgramEntity.Builder(metaData, ProgramEntity.ProgramMode.RNS, 1600, 600, 20)
                    .withSham(false)
                    .withShamDuration(25)
                    .withBipolar(false)
                    .withFrequency(1000L)
                    .withRandomCurrent(true)
                    .withRandomFrequency(false)
                    .build();
        }

        return noiseProgram;
    }
}
