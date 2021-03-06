// This file is part of Zombiepolis
// Zombiepolis is based on MicropolisJ 
// Copyright (C) 2014 Brier Hylen Field, Max Marcuse, Sophie Arana, Ahlam Ismail, Carsten Keller, Nina Gerber, Anthea Wiederspohn  for Zombiepolis
// Portions Copyright (C) 2013 Jason Long for MicropolisJ
// Portions Copyright (C) 1989-2007 Electronic Arts Inc.
//
// Zombiepolis is free software; you can redistribute it and/or modify
// it under the terms of the GNU GPLv3, with additional terms.
// See the README file, included in this distribution, for details.


package micropolisj.gui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import micropolisj.engine.*;
import static micropolisj.gui.MainWindow.EXTENSION;

public class NewCityDialog extends JDialog
{
	Micropolis engine;
	JButton previousMapBtn;
	Stack<Micropolis> previousMaps = new Stack<Micropolis>();
	Stack<Micropolis> nextMaps = new Stack<Micropolis>();
	OverlayMapView mapPane;
	HashMap<Integer,JRadioButton> levelBtns = new HashMap<Integer,JRadioButton>();
	HashMap<Integer,JRadioButton> levelBtns2 = new HashMap<Integer,JRadioButton>();

	static final ResourceBundle strings = MainWindow.strings;

	public NewCityDialog(MainWindow owner, boolean showCancelOption)
	{
		super(owner);
		setTitle(strings.getString("welcome.caption"));
		setModal(true);

		assert owner != null;

		JPanel p1 = new JPanel(new BorderLayout());
		p1.setBorder(BorderFactory.createEmptyBorder(10,20,10,20));
		getContentPane().add(p1, BorderLayout.CENTER);

		engine = new Micropolis();
		new MapGenerator(engine).generateNewCity();

		mapPane = new OverlayMapView(engine);
		mapPane.setBorder(BorderFactory.createLoweredBevelBorder());
		p1.add(mapPane, BorderLayout.WEST);

		JPanel p2 = new JPanel(new BorderLayout());
		p1.add(p2, BorderLayout.CENTER);

		Box levelBox = new Box(BoxLayout.Y_AXIS);
		levelBox.setBorder(BorderFactory.createEmptyBorder(0,10,0,10));
		p2.add(levelBox, BorderLayout.CENTER);

		levelBox.add(new Label("Select Game Difficulty"));

		levelBox.add(Box.createVerticalGlue());
		JRadioButton radioBtn;
		for (int lev = GameLevel.MIN_LEVEL; lev <= GameLevel.MAX_LEVEL; lev++)
		{
			final int x = lev;
			radioBtn = new JRadioButton(strings.getString("menu.difficulty."+lev));
			radioBtn.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					setGameLevel(x);
				}});
			levelBox.add(radioBtn);
			levelBtns.put(lev, radioBtn);
		}
		levelBox.add(Box.createVerticalGlue());
		setGameLevel(GameLevel.MIN_LEVEL);
		
		levelBox.add(new Label("Select Zombie Difficulty"));
		
		// ZWEITER SCHWIERIGKEITSGRAD
		levelBox.add(Box.createVerticalGlue());
		JRadioButton radioBtn2;
		for (int lev2 = ZombLevel.MIN_LEVEL; lev2 <= ZombLevel.MAX_LEVEL; lev2++)
		{
			final int y = lev2;
			radioBtn2 = new JRadioButton(strings.getString("menu.zombieculty."+lev2));
			radioBtn2.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					setZombieLevel(y);
				}});
			levelBox.add(radioBtn2);
			levelBtns2.put(lev2, radioBtn2);
		}
		levelBox.add(Box.createVerticalGlue());
		setZombieLevel(ZombLevel.STD_LEVEL);

		JPanel buttonPane = new JPanel();
		getContentPane().add(buttonPane, BorderLayout.SOUTH);

		JButton btn;
		btn = new JButton(strings.getString("welcome.previous_map"));
		btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				onPreviousMapClicked();
			}});
		btn.setEnabled(false);
		buttonPane.add(btn);
		previousMapBtn = btn;

		btn = new JButton(strings.getString("welcome.play_this_map"));
		btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				onPlayClicked();
			}});
		buttonPane.add(btn);
		getRootPane().setDefaultButton(btn);

		btn = new JButton(strings.getString("welcome.next_map"));
		btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				onNextMapClicked();
			}});
		buttonPane.add(btn);


		pack();
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(owner);
		getRootPane().registerKeyboardAction(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				dispose();
			}},
			KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
			JComponent.WHEN_IN_FOCUSED_WINDOW);
	}

	private void onPreviousMapClicked()
	{
		if (previousMaps.isEmpty())
			return;

		nextMaps.push(engine);
		engine = previousMaps.pop();
		mapPane.setEngine(engine);

		previousMapBtn.setEnabled(!previousMaps.isEmpty());
	}

	private void onNextMapClicked()
	{
		if (nextMaps.isEmpty())
		{
			Micropolis m = new Micropolis();
			new MapGenerator(m).generateNewCity();
			nextMaps.add(m);
		}

		previousMaps.push(engine);
		engine = nextMaps.pop();
		mapPane.setEngine(engine);

		previousMapBtn.setEnabled(true);
	}

	void startPlaying(Micropolis newEngine, File file)
	{
		MainWindow win = (MainWindow) getOwner();
		win.setEngine(newEngine);
		if (newEngine.oldMapLoaded < 0)
		{
			win.oldLoad();
		}
		win.currentFile = file;
		win.makeClean();
		dispose();
	}

	private void onPlayClicked()
	{
		engine.setGameLevel(getSelectedGameLevel());
		engine.setZombLevel(getSelectedZombLevel());
		engine.setFunds(GameLevel.getStartingFunds(engine.gameLevel));
		startPlaying(engine, null);
	}

	private void onCancelClicked()
	{
		dispose();
	}

	private void onQuitClicked()
	{
		System.exit(0);
	}

	private int getSelectedGameLevel()
	{
		for (int lev : levelBtns.keySet())
		{
			if (levelBtns.get(lev).isSelected()) {
				return lev;
			}
		}
		return GameLevel.MIN_LEVEL;
	}
	
	private int getSelectedZombLevel()
	{
		for (int lev :levelBtns2.keySet())
		{
			if (levelBtns2.get(lev).isSelected())
			{
				return lev;
			}
		}
		return ZombLevel.MIN_LEVEL;
	}

	private void setGameLevel(int level)
	{
		for (int lev : levelBtns.keySet())
		{
			levelBtns.get(lev).setSelected(lev == level);
		}
	}
	
	private void setZombieLevel(int level)
	{
		for (int lev : levelBtns2.keySet())
		{
			levelBtns2.get(lev).setSelected(lev == level);
		}
	}
	
}
