package com.chaosthedude.explorerscompass.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.chaosthedude.explorerscompass.ExplorersCompass;
import com.chaosthedude.explorerscompass.items.ExplorersCompassItem;
import com.chaosthedude.explorerscompass.network.SearchPacket;
import com.chaosthedude.explorerscompass.network.TeleportPacket;
import com.chaosthedude.explorerscompass.sorting.ISorting;
import com.chaosthedude.explorerscompass.sorting.NameSorting;
import com.chaosthedude.explorerscompass.util.CompassState;
import com.chaosthedude.explorerscompass.util.StructureUtils;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.TranslatableText;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.StructureFeature;

@Environment(EnvType.CLIENT)
public class ExplorersCompassScreen extends Screen {

	private World world;
	private PlayerEntity player;
	private List<StructureFeature<?>> allowedStructures;
	private List<StructureFeature<?>> structuresMatchingSearch;
	private ItemStack stack;
	private ExplorersCompassItem explorersCompass;
	private ButtonWidget startSearchButton;
	private ButtonWidget sortByButton;
	private ButtonWidget teleportButton;
	private ButtonWidget cancelButton;
	private TransparentTextField searchTextField;
	private StructureSearchList selectionList;
	private ISorting sortingCategory;

	public ExplorersCompassScreen(World world, PlayerEntity player, ItemStack stack, ExplorersCompassItem explorersCompass, List<StructureFeature<?>> allowedStructures) {
		super(new TranslatableText("string.explorerscompass.selectStructure"));
		this.world = world;
		this.player = player;
		this.stack = stack;
		this.explorersCompass = explorersCompass;
		this.allowedStructures = allowedStructures;

		structuresMatchingSearch = new ArrayList<StructureFeature<?>>(allowedStructures);
		sortingCategory = new NameSorting();
	}

	@Override
	public boolean mouseScrolled(double scroll1, double scroll2, double scroll3) {
		return selectionList.mouseScrolled(scroll1, scroll2, scroll3);
	}

	@Override
	protected void init() {
		client.keyboard.setRepeatEvents(true);
		clearChildren();
		setupButtons();
		setupTextFields();
		if (selectionList == null) {
			selectionList = new StructureSearchList(this, client, width + 110, height, 40, height, 45);
		}
		addDrawableChild(selectionList);
	}

	@Override
	public void tick() {
		searchTextField.tick();
		teleportButton.active = explorersCompass.getState(stack) == CompassState.FOUND;
		
		// Check if the allowed structure list has synced
		if (allowedStructures.size() != ExplorersCompass.allowedStructures.size()) {
			remove(selectionList);
			allowedStructures = ExplorersCompass.allowedStructures;
			structuresMatchingSearch = new ArrayList<StructureFeature<?>>(allowedStructures);
			selectionList = new StructureSearchList(this, client, width + 110, height, 40, height, 45);
			addDrawableChild(selectionList);
		}
	}

	@Override
	public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
		renderBackground(stack);
		selectionList.render(stack, mouseX, mouseY, partialTicks);
		searchTextField.render(stack, mouseX, mouseY, partialTicks);
		drawCenteredText(stack, textRenderer, title, 65, 15, 0xffffff);
		super.render(stack, mouseX, mouseY, partialTicks);
	}

	@Override
	public boolean keyPressed(int par1, int par2, int par3) {
		boolean ret = super.keyPressed(par1, par2, par3);
		if (searchTextField.isFocused()) {
			processSearchTerm();
			return true;
		}
		return ret;
	}

	@Override
	public boolean charTyped(char typedChar, int keyCode) {
		boolean ret = super.charTyped(typedChar, keyCode);
		if (searchTextField.isFocused()) {
			processSearchTerm();
			return true;
		}
		return ret;
	}

	@Override
	public void onClose() {
		super.onClose();
		client.keyboard.setRepeatEvents(false);
	}

	public void selectStructure(StructureSearchEntry entry) {
		boolean enable = entry != null;
		startSearchButton.active = enable;
	}

	public void searchForStructure(StructureFeature<?> structure) {
		ClientPlayNetworking.send(SearchPacket.ID, new SearchPacket(StructureUtils.getIDForStructure(structure), player.getBlockPos()));
		client.setScreen(null);
	}

	public void teleport() {
		ClientPlayNetworking.send(TeleportPacket.ID, new TeleportPacket());
		client.setScreen(null);
	}

	public void processSearchTerm() {
		structuresMatchingSearch = new ArrayList<StructureFeature<?>>();
		for (StructureFeature<?> structure : allowedStructures) {
			if (StructureUtils.getStructureName(structure).toLowerCase().contains(searchTextField.getText().toLowerCase())) {
				structuresMatchingSearch.add(structure);
			}
		}
		selectionList.refreshList();
	}

	public List<StructureFeature<?>> sortStructures() {
		final List<StructureFeature<?>> structures = structuresMatchingSearch;
		Collections.sort(structures, new NameSorting());
		Collections.sort(structures, sortingCategory);
		return structures;
	}

	private void setupButtons() {
		startSearchButton = addDrawableChild(new TransparentButton(10, 40, 110, 20, new TranslatableText("string.explorerscompass.startSearch"), (onPress) -> {
			if (selectionList.hasSelection()) {
				selectionList.getSelectedOrNull().searchForBiome();
			}
		}));
		sortByButton = addDrawableChild(new TransparentButton(10, 65, 110, 20, new TranslatableText("string.explorerscompass.sortBy").append(": " + sortingCategory.getLocalizedName()), (onPress) -> {
			sortingCategory = sortingCategory.next();
			sortByButton.setMessage(new TranslatableText("string.explorerscompass.sortBy").append(": " + sortingCategory.getLocalizedName()));
			selectionList.refreshList();
		}));
		cancelButton = addDrawableChild(new TransparentButton(10, height - 30, 110, 20, new TranslatableText("gui.cancel"), (onPress) -> {
			client.setScreen(null);
		}));
		teleportButton = addDrawableChild(new TransparentButton(width - 120, 10, 110, 20, new TranslatableText("string.explorerscompass.teleport"), (onPress) -> {
			teleport();
		}));

		startSearchButton.active = false;

		teleportButton.visible = ExplorersCompass.canTeleport;
	}

	private void setupTextFields() {
		searchTextField = new TransparentTextField(textRenderer, width / 2 - 82, 10, 140, 20, new TranslatableText("string.explorerscompass.search"));
		addDrawableChild(searchTextField);
	}

}
